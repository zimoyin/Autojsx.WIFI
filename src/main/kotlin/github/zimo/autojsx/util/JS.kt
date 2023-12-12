import com.caoccao.javet.enums.JSRuntimeType
import com.caoccao.javet.interception.logging.JavetStandardConsoleInterceptor
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.interop.V8Host
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.interop.callback.JavetCallbackContext
import com.caoccao.javet.interop.converters.*
import com.caoccao.javet.interop.engine.IJavetEngine
import com.caoccao.javet.interop.engine.JavetEngineConfig
import com.caoccao.javet.interop.engine.JavetEnginePool
import com.caoccao.javet.interop.executors.IV8Executor
import com.caoccao.javet.node.modules.NodeModuleModule
import com.caoccao.javet.values.V8Value
import com.caoccao.javet.values.primitive.V8ValuePrimitive
import com.caoccao.javet.values.reference.V8ValueObject
import org.intellij.lang.annotations.Language
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Method
import java.math.BigInteger
import java.time.ZonedDateTime


/**
 * 引入JS V8 引擎依赖
 * ```
 *         <!-- Linux and Windows (x86_64) -->
 *         <dependency>
 *             <groupId>com.caoccao.javet</groupId>
 *             <artifactId>javet</artifactId>
 *             <version>3.0.1</version>
 *         </dependency>
 *
 *         <!-- Linux (arm64) -->
 *         <dependency>
 *             <groupId>com.caoccao.javet</groupId>
 *             <artifactId>javet-linux-arm64</artifactId>
 *             <version>3.0.1</version>
 *         </dependency>
 *
 *         <!-- Mac OS (x86_64 and arm64) -->
 *         <dependency>
 *             <groupId>com.caoccao.javet</groupId>
 *             <artifactId>javet-macos</artifactId>
 *             <version>3.0.1</version>
 *         </dependency>
 *
 *          <!-- 安卓依赖这里不给出了 -->
 * ```
 *
 * 1. 通过全局函数获取引擎或者引擎域
 * ```
 *  NodeJsWithPool{}   通过 NodeJS 引擎池获取可用的 Node v8 引擎
 *  V8WithPool{}      通过 V8 引擎池获取可用的 V8 引擎
 *  NodeJs{}          通过 NodeJS 引擎获取一个 Node v8 引擎
 *  V8{}              通过 V8 引擎获取一个 V8 引擎
 * ```
 * 2. 常用方法
 * NodeRuntime.setNodeModuleRootDirectory("./node_modules") //设置 node 的模块地址
 * V8Runtime.getObjectConverter().setLargeScope()        //设置全局对象转换器转换范围
 * V8Runtime.setGlobalObject("map", test)                  //设置Java 对象 刀 JS的全局对象中
 * NodeRuntime.require("./obfuscator.js")                  //引入模块
 * V8Runtime.loadJsFile("./obfuscator.js")                  //引入 js 文件
 * V8Runtime.execute..()                                    //执行js代码
 * V8Runtime.invoke..()                                    //执行js 运行时中的方法
 * V8Runtime.injection(class,name)                        //将该类注册到 js 运行时中
 * V8Runtime.injectionFunction()
 * V8Runtime.interception()                               //将对象注册到 js 运行时中，该对象允许互操作
 * V8Runtime.openConsole()                                 //打开控制台 注意控制台需要关闭，否则会泄漏
 *
 * 3. javet 是支持动态或者自定义加载 v8/node.dll 的 详细见
 * @link [https://www.caoccao.com/Javet/reference/resource_management/load_and_unload.html]
 */

/**
 * JS 引擎池
 */
private var javetV8EnginePool: JavetEnginePool<V8Runtime>? = null
private var javetNodeEnginePool: JavetEnginePool<NodeRuntime>? = null

/**
 * 获取引擎池
 */
private fun getJavetEnginePool(type: JSRuntimeType = JSRuntimeType.V8): JavetEnginePool<out V8Runtime> {
    return if (type == JSRuntimeType.Node) {
        if (javetV8EnginePool == null || javetV8EnginePool!!.isClosed || !javetV8EnginePool!!.isActive || javetV8EnginePool!!.isQuitting) {
            javetV8EnginePool = createJavetV8EnginePool()
            javetV8EnginePool = createJavetV8EnginePool()
        }
        javetV8EnginePool!!
    } else {
        if (javetNodeEnginePool == null || javetNodeEnginePool!!.isClosed || !javetNodeEnginePool!!.isActive || javetNodeEnginePool!!.isQuitting) {
            javetNodeEnginePool = createJavetNodeEnginePool()
        }
        javetNodeEnginePool!!
    }
}


/**
 * 创建一个新 V8 的引擎池
 */
fun createJavetV8EnginePool(): JavetEnginePool<V8Runtime> {
    return JavetEnginePool<V8Runtime>()
}

/**
 * 创建一个新Node 的引擎池
 */
fun createJavetNodeEnginePool(): JavetEnginePool<NodeRuntime> {
    return JavetEnginePool<NodeRuntime>()
}

/**
 * 从引擎池获取一个指定类型的引擎
 */
fun getJavetEngine(
    type: JSRuntimeType = JSRuntimeType.V8,
    config: (JavetEngineConfig) -> Unit = {},
): IJavetEngine<out V8Runtime> {
    val javetEnginePool = getJavetEnginePool(type)
    // 设置引擎为 V8 或者 NODE
    javetEnginePool.config.setJSRuntimeType(type)
    config(javetEnginePool.config)
    return javetEnginePool.engine
}

/**
 * 关闭引擎池
 */
fun javetEnginePoolClose(type: JSRuntimeType? = null) {
    when (type) {
        JSRuntimeType.Node -> {
            if (javetNodeEnginePool != null) javetNodeEnginePool?.close()
            javetNodeEnginePool = null
        }

        JSRuntimeType.V8 -> {
            if (javetV8EnginePool != null) javetV8EnginePool?.close()
            javetV8EnginePool = null
        }

        else -> {
            if (javetNodeEnginePool != null) javetNodeEnginePool?.close()
            javetNodeEnginePool = null
            if (javetV8EnginePool != null) javetV8EnginePool?.close()
            javetV8EnginePool = null
        }
    }
}

/**
 * 从引擎池获取一个 V8类型的引擎
 */
fun getV8(config: (JavetEngineConfig) -> Unit = {}): IJavetEngine<out V8Runtime> {
    return getJavetEngine(JSRuntimeType.V8, config)
}

/**
 * 从引擎池获取一个 Node类型的引擎
 */
fun getNode(config: (JavetEngineConfig) -> Unit = {}): IJavetEngine<out V8Runtime> {
    return getJavetEngine(JSRuntimeType.Node, config)
}

/**
 * 从引擎池获取一个 V8类型的引擎
 * @param timeoutMillis 脚本执行的超时时间，默认为0,最小值为 1000 不限制 [实验性参数]
 * bug: 当执行完毕代码后，没有超过超时时间不会立即关闭引擎，因为有个守护线程在运行 他可能在2分钟之内结束
 * @param enableInDebugMode 在Debug 的时候也开启超时统计 [实验性参数]
 * @param scope 引擎执行回调
 */
fun V8WithPool(
    timeoutMillis: Long = 0,
    enableInDebugMode: Boolean = false,
    scope: V8Runtime.(V8Runtime) -> Unit,
) {
    getJavetEngine(JSRuntimeType.V8).apply {
        if (timeoutMillis >= 1000) {
            getGuard(timeoutMillis)
            if (enableInDebugMode) {
                guard.enableInDebugMode()
            }
        }
        try {
            v8Runtime.scope(this.v8Runtime)
        } finally {
            this.v8Runtime.lowMemoryNotification()
            if (timeoutMillis >= 1000) guard.close()
        }
    }
}

/**
 * 从引擎池获取一个 Node类型的引擎
 * @param timeoutMillis 脚本执行的超时时间，默认为0,最小值为 1000 不限制 [实验性参数]
 * bug: 当执行完毕代码后，没有超过超时时间不会立即关闭引擎，因为有个守护线程在运行 他可能在2分钟之内结束
 * @param enableInDebugMode 在Debug 的时候也开启超时统计 [实验性参数]
 * @param scope 引擎执行回调
 */
fun NodeJsWithPool(
    timeoutMillis: Long = 0,
    enableInDebugMode: Boolean = false,
    scope: NodeRuntime.(V8Runtime) -> Unit,
) {
    getJavetEngine(JSRuntimeType.Node).apply {
        if (timeoutMillis >= 1000) {
            getGuard(timeoutMillis)
            if (enableInDebugMode) {
                guard.enableInDebugMode()
            }
        }
        try {
            (this.v8Runtime as NodeRuntime).scope(this.v8Runtime)
        } finally {
            this.v8Runtime.lowMemoryNotification()
            if (timeoutMillis >= 1000) guard.close()
        }
    }
}

/**
 * 获取一个 V8类型的引擎，执行完毕代码后就释放该引擎，执行完毕代码后就释放该引擎。如果想要复用请参照 V8WithPool
 * @param scope 引擎执行回调
 */
fun V8(scope: V8Runtime.(V8Runtime) -> Unit) {
    V8Host.getV8Instance().createV8Runtime<V8Runtime>().use { v8Runtime ->
        try {
            //scope: V8Runtime.(V8Runtime) 代表必须在 V8Runtime 对象下执行该方法
            v8Runtime.scope(v8Runtime)
        } finally {
            v8Runtime.lowMemoryNotification()
        }
    }
}

/**
 * 获取一个 Node类型的引擎，执行完毕代码后就释放该引擎。如果想要复用请参照 NodeJsWithPool
 * @param scope 引擎执行回调
 */
fun NodeJs(scope: NodeRuntime.(V8Runtime) -> Unit) {
    V8Host.getNodeInstance().createV8Runtime<V8Runtime>().use { v8Runtime ->
//        (v8Runtime as NodeRuntime).apply {
//           scope(v8Runtime)
//        }
        //上面 (v8Runtime as NodeRuntime).apply {scope(v8Runtime)} 等价于
        try {
            (v8Runtime as NodeRuntime).scope(v8Runtime)
        } finally {
            v8Runtime.lowMemoryNotification()
        }
    }
}

/**
 * 设置Node 引擎的模块路径
 */
fun NodeRuntime.setNodeModuleRootDirectory(path: String) {
    getNodeModule(NodeModuleModule::class.java).setRequireRootDirectory(File(path))
}

/**
 * 设置Node 引擎的模块路径
 */
fun NodeRuntime.setNodeModuleRootDirectory(path: File) {
    getNodeModule(NodeModuleModule::class.java).setRequireRootDirectory(File("./temp/node_moduies"))
}


/**
 * 获取对象转换器
 */
fun V8Runtime.getObjectConverter(): ObjectConverter {
    return ObjectConverter(this)
}

/**
 * 对象转换器设置类
 * https://www.caoccao.com/Javet/reference/converters/index.html
 */
class ObjectConverter(val v8Runtime: V8Runtime) {
    /**
     * 开启从java对象到 js对象转换的自动转换器
     * Javet 代理转换器类型通过 JS 代理双向将大多数 Java 对象转换为 JS 对象。
     * Java Primitive 类型、Array、List、Set 和 Map 在 JS 中被转换为相应的类型。可以通过配置禁用设置和地图转换。
     */
    private fun setProxy(config: (JavetConverterConfig<*>) -> Unit = {}) {
        val javetProxyConverter = JavetProxyConverter()
        config(javetProxyConverter.config)
        v8Runtime.setConverter(javetProxyConverter)
    }

    /**
     * Javet 桥接转换器类型通过 JS 代理双向将所有 Java 对象转换为 JS 对象。
     * 唯一的例外是 Java 数组被转换为 JS 数组。是 setObjectConverterWithProxy() 的增强方法
     */
    private fun setProxyBridge(config: (JavetConverterConfig<*>) -> Unit = {}) {
        val converter = JavetBridgeConverter()
        config(converter.config)
        v8Runtime.setConverter(converter)
    }

    /**
     * Javet 对象转换器将 Java 原始类型 转换为 JS 原始类型 不涉及数组、列表、映射或集合等...
     */
    private fun setPrimitiveType(config: (JavetConverterConfig<*>) -> Unit = {}) {
        val converter = JavetPrimitiveConverter()
        config(converter.config)
        v8Runtime.setConverter(converter)
    }

    /**
     * Javet 对象转换器将 Java 基元类型 Array、List、Map 和 Set 双向转换为 JS 基元类型 Array、Map、Set 和 Object。
     */
    private fun setPrimitiveObjectType(config: (JavetConverterConfig<*>) -> Unit = {}) {
        val converter = JavetObjectConverter()
        config(converter.config)
        v8Runtime.setConverter(converter)
    }

    /**
     * 设置小范围的转换，只转换基础数据类型
     */
    fun setSmallScope(config: (JavetConverterConfig<*>) -> Unit = {}) {
        setPrimitiveType(config)
    }

    /**
     * 设置中等范围的转换，只转换基础数据类型与Array、List、Map 和 Set
     */
    fun setMediumScope(config: (JavetConverterConfig<*>) -> Unit = {}) {
        setPrimitiveObjectType(config)
    }

    /**
     * 设置大范围的转换，包括基础数据类型、Array、List、Map 和 Set 与 大部分对象
     */
    fun setLargeScope(config: (JavetConverterConfig<*>) -> Unit = {}) {
        setProxy(config)
    }

    /**
     * 设置最大范围的转换，包括基础数据类型、Array、List、Map 和 Set 与 大部分对象。并支持双向转换
     */
    fun setLargerScope(config: (JavetConverterConfig<*>) -> Unit = {}) {
        setProxyBridge(config)
    }

    /**
     * 获取转换等级，等级从 1-4
     *
     * JavetPrimitiveConverter   ->   1  设置小范围的转换，只转换基础数据类型
     * JavetObjectConverter      ->   2  设置中等范围的转换，只转换基础数据类型与Array、List、Map 和 Set
     * JavetProxyConverter       ->   3  设置大范围的转换，包括基础数据类型、Array、List、Map 和 Set 与 大部分对象
     * JavetBridgeConverter      ->   4  设置最大范围的转换，包括基础数据类型、Array、List、Map 和 Set 与 大部分对象。并支持双向转换
     */
    fun getLevel(): Int {
        when (v8Runtime.converter::class.java) {
            JavetPrimitiveConverter::class.java -> return 1
            JavetObjectConverter::class.java -> return 2
            JavetProxyConverter::class.java -> return 3
            JavetBridgeConverter::class.java -> return 4
        }
        return 0
    }
}

/**
 * 添加对象到 JS 执行环境的全局对象中
 * @param valueName 全局对象名称
 * @param obj java 对象
 */
fun V8Runtime.setGlobalObject(valueName: Any, obj: Any): Boolean {
    val v8 = this
    converter
    //转换等级过大则不做检查
    if (getObjectConverter().getLevel() >= 3) {
        return globalObject.set(valueName, obj)
    } else if (getObjectConverter().getLevel() == 1) {
        val b = when (obj) {
            is Int, is Double, is Char, is Boolean, is String, is Long, is Short, is Float -> true
            else -> false
        }.apply {
            if (!this){
                v8.logger.error("Cannot convert objects outside of the base type into the JS runtime. Please set the object converter to JavetObject Converter, JavetProxyConverter, or JavetBridgeConverter; Eg: V8Runtime. setConverter (....)")
            }
        }
        return globalObject.set(valueName, obj) && b
    } else {
        val b = when (obj) {
            is Int, is Double, is Char, is Boolean, is String, is Long, is Short, is Float -> true
            is Array<*> -> true
            is List<*>, is Map<*, *>, is Set<*> -> true
            is Collection<*> -> true
            else -> false
        }.apply {
            if (!this){
                v8.logger.error("Cannot convert objects outside of the base type into the JS runtime. Please set the object converter to JavetProxyConverter or JavetBridgeConverter. Eg: V8Runtime. setConverter (....)")
            }
        }
        return globalObject.set(valueName, obj) && b
    }
}

/**
 * 添加对象到 JS 执行环境的全局对象中
 * @param valueName 全局对象名称
 */
fun V8Runtime.removeGlobalObject(valueName: Any): Boolean {
    return globalObject.delete(valueName)
}

/**
 * 对象拦截器，将JAVA 对象 与 JS中的对象进行绑定，当任意一方修改后，其他一方都会修改，并且 js 被允许调用该JAVA对象的方法
 * 其中核心逻辑在于 V8ValueObject.bind(Object)
 *
 * @param name 将该对象注册到 js 中的名称
 * @param objectMap 拦截器，注意该拦截器要使用 @V8Property 注解标注 属性的 get/set 方法，使用 @V8Function 注解标注要向JS暴露的方法
 * @param v8Value - V8值对象，默认为空，表示创建新的V8值对象。
 * @param forceDeclareExistingObject - 是否声明已存在的V8值对象，默认为false。
 *
 * @link 文档[https://www.caoccao.com/Javet/tutorial/basic/interception.html]
 */
fun V8Runtime.interceptionObject(
    name: String,
    objectMap: Any,
    v8Value: V8ValueObject? = null,
    forceDeclareExistingObject: Boolean = false,
): MutableList<JavetCallbackContext>? {
    if (v8Value == null) {
        createV8ValueObject().use { v8ValueObject ->
            //声明这个对象
            globalObject[name] = v8ValueObject
            //将这个对象与JAVA对象进行绑定
            return v8ValueObject.bind(objectMap)
        }
    } else {
        if (forceDeclareExistingObject) globalObject[objectMap.javaClass.simpleName.lowercase()] = v8Value
        return v8Value.bind(objectMap)
    }
}

/**
 * 对象拦截器，将JAVA 对象 与 JS中的对象进行绑定，当任意一方修改后，其他一方都会修改，并且 js 被允许调用该JAVA对象的方法
 * 注意：将根据传入的拦截器的 类名进行注册到 js 运行时中
 * @param objectMap 拦截器，注意该拦截器要使用 @V8Property 注解标注 属性的 get/set 方法，使用 @V8Function 注解标注要向JS暴露的方法
 * @param v8ValueName - V8值对象的名称，默认为方法所属类的类名小写。
 * @param v8Value - V8值对象，默认为空，表示创建新的V8值对象。
 * @param forceDeclareExistingObject - 是否声明已存在的V8值对象，默认为false。
 */
fun V8Runtime.interceptionObject(
    objectMap: Any,
    v8ValueName: String = objectMap.javaClass.simpleName.lowercase(),
    v8Value: V8ValueObject? = null,
    forceDeclareExistingObject: Boolean = false,
): MutableList<JavetCallbackContext>? {
    if (v8Value == null) {
        createV8ValueObject().use { v8ValueObject ->
            //声明这个对象
            globalObject[v8ValueName] = v8ValueObject
            //将这个对象与JAVA对象进行绑定
            return v8ValueObject.bind(objectMap)
        }
    } else {
        if (forceDeclareExistingObject) globalObject[objectMap.javaClass.simpleName.lowercase()] = v8Value
        return v8Value.bind(objectMap)
    }
}

/**
 * interceptionFunction 是一个用于在 V8Runtime 中拦截方法调用的函数。为该对象注册一个Java 的方法
 * 调用方式为 该方法所在的 类名(小写).方法名()
 *  BaseJavetConsoleInterceptor 便是操作类似方法将 js 的函数拦截并重定向到java 的实现者。值得一提的是如果开启了对象转换可用将js传入的参数转为java可用使用的参数，而不用使用 V8Value 的封装
 * @param methodOwnerClassObject - 拥有要拦截的方法的对象。
 * @param method - 要拦截的方法。
 * @param v8ValueName - V8值对象的名称，默认为方法所属类的类名小写。
 * @param v8Value - V8值对象，默认为空，表示创建新的V8值对象。
 * @param forceDeclareExistingObject - 是否声明已存在的V8值对象，默认为false。
 */
fun V8Runtime.interceptionFunction(
    methodOwnerClassObject: Any,
    method: Method,
    v8ValueName: String = methodOwnerClassObject.javaClass.simpleName.lowercase(),
    v8Value: V8ValueObject? = null,
    forceDeclareExistingObject: Boolean = false,
): Boolean {
    if (v8Value == null) {
        createV8ValueObject().use { v8ValueObject ->
            //声明这个对象
            globalObject[v8ValueName] = v8ValueObject
            //将这个对象与JAVA对象进行绑定
            return v8ValueObject.bindFunction(
                JavetCallbackContext(
                    method.name,
                    methodOwnerClassObject,
                    method
                )
            )
        }
    } else {
        v8Value.let { v8ValueObject ->
            //声明这个对象
            if (forceDeclareExistingObject) globalObject[v8ValueName] = v8ValueObject
            //将这个对象与JAVA对象进行绑定
            return v8ValueObject.bindFunction(
                JavetCallbackContext(
                    method.name,
                    methodOwnerClassObject,
                    method
                )
            )
        }
    }
}

/**
 * interceptionFunction 是一个用于在 V8Runtime 中拦截方法调用的函数。为该对象注册一个Java 的方法 或者 为已经存在的js 方法拦截执行并重定向到java 的方法
 * 调用方式为 该方法所在的 类名(小写).方法名()
 *  BaseJavetConsoleInterceptor 便是操作类似方法将 js 的函数拦截并重定向到java 的实现者。值得一提的是如果开启了对象转换可用将js传入的参数转为java可用使用的参数，而不用使用 V8Value 的封装
 * @param method - 要拦截的方法。
 * @param v8ValueName - V8值对象的名称，默认为方法所属类的类名小写。
 * @param v8Value - V8值对象，默认为空，表示创建新的V8值对象。
 * @param forceDeclareExistingObject - 是否声明已存在的V8值对象，默认为false。
 */
fun V8Runtime.interceptionFunction(
    method: Method,
    v8ValueName: String = method.declaringClass.javaClass.simpleName.lowercase(),
    v8Value: V8ValueObject? = null,
    forceDeclareExistingObject: Boolean = false,
) {
    val cls = method.declaringClass
    val constructor = cls.getDeclaredConstructor().apply {
        isAccessible = true
    }
    val obj = constructor.newInstance()
    if (v8Value == null) {
        createV8ValueObject().use { v8ValueObject ->
            //声明这个对象
            globalObject[v8ValueName] = v8ValueObject
            //将这个对象与JAVA对象进行绑定
            v8ValueObject.bindFunction(
                JavetCallbackContext(
                    method.name,
                    obj,
                    method
                )
            )
        }
    } else {
        v8Value.let { v8ValueObject ->
            //声明这个对象
            if (forceDeclareExistingObject) globalObject[v8ValueName] = v8ValueObject
            //将这个对象与JAVA对象进行绑定
            v8ValueObject.bindFunction(
                JavetCallbackContext(
                    method.name,
                    obj,
                    method
                )
            )
        }
    }
}


/**
 * 将参数列表注入到JS中的函数参数列表中
 * v8.injectionFunction("main",File("./pom.xml"));
 * 以下为JS脚本代码:
 * ```js
 *  function main(pattern) {
 *     console.log(pattern.exists())
 *  }
 * ```
 */
fun <T : Any> V8Runtime.injectionFunction(functionName: String, vararg values: Any) {
    globalObject.invokeObject<T>(functionName, values)
}


/**
 * 将类 注入到JS执行环境中
 * <br/>
 * 注意：需要开启 openObjectConverter() <br/>
 * 并且 不能注入已经被new( 数组/map/list/其他数据类型 除外) 的对象,这可能需要拦截器进行工作
 * 对于如果你需要注入一个已经被 new 的对象，你需要保证他是( 数组/map/list/其他数据类型 ) 任意一个。
 * 如果不是就尽可能不去调用该对象的方法,因为可能因为方法参数传值不正确而导致发生异常
 *
 * 注意: 如果传入的是Class则无需要注意，如果传入的是对象则'可能'需要拦截器辅助进行工作,如果是JSON 对象 则不推荐拦截器而是推荐(JsonNode) IJavetDirectProxyHandler<Exception>
 *
 * v8.injection(['1','2'],"array01");
 * v8.injection(File("./src"),"file");
 * v8.injection(System::class.java,"System");
 * 以下为JS脚本代码:
 * ```js
 *      // 可以直接在JavaScript中调用Java引用。
 *      System.out.println('Hello from Java');
 *      // 可以将Java引用直接分配给JavaScript变量。
 *      const println = System.out.println;
 *      // 可以将Java引用直接分配给JavaScript变量。
 *      println('Hello from JavaScript');
 *      //如果注入  StringBuilder 可以使用 new 来创建对象
 *      new StringBuilder('Hello').append(' from StringBuilder').toString()
 *      //可以调用列表之外的对象，但是如果调用该对象的方法可能会报错，通常是因为传值不正确导致的
 *      println(file.path);
 *      //如果该对象的属性可以允许被修改则可以通过js 进行修改
 *      file.path = '123'
 * ```
 */
fun V8Runtime.injection(cls: Class<*>, valueName: String = cls.simpleName) {
    globalObject.set(valueName, cls)
}

/**
 * 清理全局对象，通常用于清理被注入到js环境中的java对象
 */
fun V8Runtime.removal(valueName: String) {
    globalObject.delete(valueName)
}

/**
 * 释放该引擎使用的内存
 */
fun V8Runtime.gc() {
    this.lowMemoryNotification()
}

/**
 * 开启控制台，并自动关闭
 */
fun V8Runtime.useConsole(scope: (V8Runtime) -> Unit) {
    val console = this.openConsole()
    try {
        scope(this)
    } finally {
        console.closeConsole()
    }
}

/**
 * 开启该控制台,注意要手动关闭控制台，否则可能会造成内存泄漏
 * 通过 setInfo,setWarn,setError。。。 可以重定向输出流.
 *
 * 这个控制台无法输出对象里面的内容，如果想要输出对象内容，请继承 JavetStandardConsoleInterceptor 类 并实现 consoleLog。。。等此类方法，判断并将对象转为json 之后再输出
 */
fun V8Runtime.openConsole(): JavetStandardConsoleInterceptor {
    val javetConsoleInterceptor = JavetStandardConsoleInterceptor(this)
    // 将Javet控制台注册到V8全局对象。
    javetConsoleInterceptor.register(this.globalObject)
    return javetConsoleInterceptor
}

/**
 * 关闭该控制台
 */
fun JavetStandardConsoleInterceptor.closeConsole(v8: V8Runtime? = null) {
    if (v8 == null) {
        this.unregister(v8Runtime.globalObject)
    } else {
        this.unregister(v8.globalObject)
    }
}

/**
 * 关闭该控制台
 */
fun JavetStandardConsoleInterceptor.use(scope: (V8Runtime) -> Unit) {
    try {
        scope(this.v8Runtime)
    } finally {
        this.closeConsole()
    }
}

/**
 * 在当前JS 执行环境中加载 js 文件
 */
fun V8Runtime.loadJsFile(jsFile: InputStream) {
    try {
        getExecutor(String(jsFile.readAllBytes())).executeVoid()
    } catch (e: Exception) {
        throw IOException("Unable to load JS file: $jsFile", e)
    }
}


/**
 * 在当前JS 执行环境中加载 js 文件
 */
fun V8Runtime.loadJsFile(jsFile: File) {
    if (jsFile.exists() && jsFile.canRead()) {
        try {
            getExecutor(jsFile).executeVoid()
        } catch (e: Exception) {
            throw IOException("Unable to load JS file: $jsFile", e)
        }
    } else {
        throw IOException("Unable to load JS file: $jsFile")
    }
}

/**
 * 在当前JS 执行环境中加载 js 文件
 */
fun V8Runtime.loadJsFile(jsFilePath: String) {
    val jsFile = File(jsFilePath)
    loadJsFile(jsFilePath)
}

private fun extractFileNamePrefix(fileName: String): String {
    val dotIndex = fileName.indexOf('.')

    return if (dotIndex != -1) {
        fileName.substring(0, dotIndex)
    } else {
        fileName
    }
}

/**
 * 加载JS 执行环境中加载 js 文件
 * @param jsFilePath js 文件路径
 * @param moduleName 接收该文件的变量名称，默认是这文件的名称
 */
fun V8Runtime.require(jsFile: File, moduleName: String? = null) {
    if (jsFile.exists() && jsFile.canRead()) {
        setGlobalObject("path", jsFile.path)
        try {
            executeString(
                """
               let ${moduleName ?: extractFileNamePrefix(jsFile.name)} = require(path)
        """.trimIndent()
            )
        } catch (e: Exception) {
            throw IOException("Unable to load JS file: $jsFile", e)
        } finally {
            removeGlobalObject("path")
        }
    } else {
        throw IOException("Unable to load JS file: $jsFile")
    }
}

/**
 * 再JS 执行环境中加载 js 文件
 * @param jsFilePath js 文件路径
 * @param moduleName 接收该文件的变量名称，默认是这文件的名称
 */
fun V8Runtime.require(jsFilePath: String, moduleName: String? = null) {
    val jsFile = File(jsFilePath)
    require(jsFile, moduleName)
}

/**
 * 执行来自js环境中的全局方法,该泛型就是返回值类型
 * 如果像执行来自某个值或者对象下的方法，你需要获取该方法只会找到 invoke 系列方法既可以执行
 */
fun <T : V8Value> V8Runtime.invoke(functionName: String, vararg arguments: Any): T {
    val value = this.globalObject.get<V8Value>(functionName)
    if (value == null || value.isNullOrUndefined) throw NullPointerException("The method cannot be found in the root space of the current execution environment: $functionName")
    value.close()
    return this.globalObject.invoke<T>(functionName, *arguments)
}

/**
 * 执行来自js环境中的全局方法,无返回值
 * 如果像执行来自某个值或者对象下的方法，你需要获取该方法只会找到 invoke 系列方法既可以执行
 */
fun V8Runtime.invoke(functionName: String, vararg arguments: Any) {
    val value = this.globalObject.get<V8Value>(functionName)
    if (value == null || value.isNullOrUndefined) throw NullPointerException("The method cannot be found in the root space of the current execution environment: $functionName")
    value.close()
    return this.globalObject.invokeVoid(functionName, *arguments)
}

/**
 * 执行来自js环境中的全局方法
 * 如果像执行来自某个值或者对象下的方法，你需要获取该方法只会找到 invoke 系列方法既可以执行
 */
fun V8Runtime.invokeVoid(functionName: String, vararg arguments: Any) {
    val value = this.globalObject.get<V8Value>(functionName)
    if (value == null || value.isNullOrUndefined) throw NullPointerException("The method cannot be found in the root space of the current execution environment: $functionName")
    value.close()
    return this.globalObject.invokeVoid(functionName, *arguments)
}


/**
 * 执行来自js环境中的全局方法
 * 如果像执行来自某个值或者对象下的方法，你需要获取该方法只会找到 invoke 系列方法既可以执行
 */
fun V8Runtime.invokeString(functionName: String, vararg arguments: Any): String? {
    val value = this.globalObject.get<V8Value>(functionName)
    if (value == null || value.isNullOrUndefined) throw NullPointerException("The method cannot be found in the root space of the current execution environment: $functionName")
    value.close()
    return this.globalObject.invokeString(functionName, *arguments)
}

/**
 * 执行来自js环境中的全局方法
 * 如果像执行来自某个值或者对象下的方法，你需要获取该方法只会找到 invoke 系列方法既可以执行
 */
fun V8Runtime.invokeInteger(functionName: String, vararg arguments: Any): Int? {
    val value = this.globalObject.get<V8Value>(functionName)
    if (value == null || value.isNullOrUndefined) throw NullPointerException("The method cannot be found in the root space of the current execution environment: $functionName")
    value.close()

    return this.globalObject.invokeInteger(functionName, *arguments)
}

/**
 * 执行来自js环境中的全局方法
 * 如果像执行来自某个值或者对象下的方法，你需要获取该方法只会找到 invoke 系列方法既可以执行
 */
fun V8Runtime.invokeDouble(functionName: String, vararg arguments: Any): Double? {
    val value = this.globalObject.get<V8Value>(functionName)
    if (value == null || value.isNullOrUndefined) throw NullPointerException("The method cannot be found in the root space of the current execution environment: $functionName")
    value.close()

    return this.globalObject.invokeDouble(functionName, *arguments)
}

/**
 * 执行来自js环境中的全局方法
 * 如果像执行来自某个值或者对象下的方法，你需要获取该方法只会找到 invoke 系列方法既可以执行
 */
fun <T : Any> V8Runtime.invokeObject(functionName: String, vararg arguments: Any): T? {
    val value = this.globalObject.get<V8Value>(functionName)
    if (value == null || value.isNullOrUndefined) throw NullPointerException("The method cannot be found in the root space of the current execution environment: $functionName")
    value.close()
    return this.globalObject.invokeObject(functionName, *arguments)
}

/**
 * 执行来自js环境中的全局方法
 * 如果像执行来自某个值或者对象下的方法，你需要获取该方法只会找到 invoke 系列方法既可以执行
 */
fun V8Runtime.invokeBoolean(functionName: String, vararg arguments: Any): Boolean? {
    val value = this.globalObject.get<V8Value>(functionName)
    if (value == null || value.isNullOrUndefined) throw NullPointerException("The method cannot be found in the root space of the current execution environment: $functionName")
    value.close()

    return this.globalObject.invokeBoolean(functionName, *arguments)
}

/**
 * 执行来自js环境中的全局方法
 * 如果像执行来自某个值或者对象下的方法，你需要获取该方法只会找到 invoke 系列方法既可以执行
 */
fun V8Runtime.invokeLong(functionName: String, vararg arguments: Any): Long? {
    val value = this.globalObject.get<V8Value>(functionName)
    if (value == null || value.isNullOrUndefined) throw NullPointerException("The method cannot be found in the root space of the current execution environment: $functionName")
    value.close()
    return this.globalObject.invokeLong(functionName, *arguments)
}

/**
 * 执行来自js环境中的全局方法
 * 如果像执行来自某个值或者对象下的方法，你需要获取该方法只会找到 invoke 系列方法既可以执行
 */
fun V8Runtime.invokeFloat(functionName: String, vararg arguments: Any): Float? {
    val value = this.globalObject.get<V8Value>(functionName)
    if (value == null || value.isNullOrUndefined) throw NullPointerException("The method cannot be found in the root space of the current execution environment: $functionName")
    value.close()
    return this.globalObject.invokeFloat(functionName, *arguments)
}


/**
 * 执行来自js环境中的全局方法
 * 如果像执行来自某个值或者对象下的方法，你需要获取该方法只会找到 invoke 系列方法既可以执行
 */
fun <R, T : V8ValuePrimitive<R>?> V8Runtime.invokePrimitive(functionName: String, vararg arguments: Any): R? {
    val value = this.globalObject.get<V8Value>(functionName)
    if (value == null || value.isNullOrUndefined) throw NullPointerException("The method cannot be found in the root space of the current execution environment: $functionName")
    value.close()
    return this.globalObject.invokePrimitive<R, T>(functionName, *arguments)
}


/**
 * 构建一个可以允许被执行的脚本对象
 * @param script js script
 * @param byteArray cachedData – the cached data
 */
fun V8Runtime.execute(@Language("JavaScript") script: String, byteArray: ByteArray? = null): IV8Executor {
    return if (byteArray != null) {
        this.getExecutor(script, byteArray);
    } else {
        this.getExecutor(script);
    }
}

/**
 * 构建并执行脚本
 * 该泛型为该js执行后返回的参数类型，默认从 V8Value 进行继承
 * @param script js script
 * @param byteArray cachedData – the cached data
 */
fun <T : V8Value> V8Runtime.execute(@Language("JavaScript") script: String, byteArray: ByteArray? = null): T? {
    return if (byteArray != null) {
        this.getExecutor(script, byteArray).execute()
    } else {
        this.getExecutor(script).execute()
    }
}

/**
 * 构建并执行脚本
 * @param script js script
 * @param byteArray cachedData – the cached data
 */
fun V8Runtime.executeString(@Language("JavaScript") script: String, byteArray: ByteArray? = null): String? {
    return if (byteArray != null) {
        this.getExecutor(script, byteArray).executeString()
    } else {
        this.getExecutor(script).executeString()
    }
}

/**
 * 构建并执行脚本
 * @param script js script
 * @param byteArray cachedData – the cached data
 */
fun V8Runtime.executeVoid(@Language("JavaScript") script: String, byteArray: ByteArray? = null) {
    if (byteArray != null) {
        this.getExecutor(script, byteArray).executeVoid()
    } else {
        this.getExecutor(script).executeVoid()
    }
}

/**
 * 构建并执行脚本
 * @param script js script
 * @param byteArray cachedData – the cached data
 */
fun V8Runtime.executeBoolean(@Language("JavaScript") script: String, byteArray: ByteArray? = null): Boolean? {
    return if (byteArray != null) {
        this.getExecutor(script, byteArray).executeBoolean()
    } else {
        this.getExecutor(script).executeBoolean()
    }
}

/**
 * 构建并执行脚本
 * @param script js script
 * @param byteArray cachedData – the cached data
 */
fun V8Runtime.executeBigInteger(
    @Language("JavaScript") script: String,
    byteArray: ByteArray? = null,
): BigInteger? {
    return if (byteArray != null) {
        this.getExecutor(script, byteArray).executeBigInteger()
    } else {
        this.getExecutor(script).executeBigInteger()
    }
}

/**
 * 构建并执行脚本
 * @param script js script
 * @param byteArray cachedData – the cached data
 */
fun V8Runtime.executeDouble(@Language("JavaScript") script: String, byteArray: ByteArray? = null): Double? {
    return if (byteArray != null) {
        this.getExecutor(script, byteArray).executeDouble()
    } else {
        this.getExecutor(script).executeDouble()
    }
}

/**
 * 构建并执行脚本
 * @param script js script
 * @param byteArray cachedData – the cached data
 */
fun V8Runtime.executeLong(@Language("JavaScript") script: String, byteArray: ByteArray? = null): Long? {
    return if (byteArray != null) {
        this.getExecutor(script, byteArray).executeLong()
    } else {
        this.getExecutor(script).executeLong()
    }
}


/**
 * 构建并执行脚本
 * @param script js script
 * @param byteArray cachedData – the cached data
 */
fun V8Runtime.executeInteger(@Language("JavaScript") script: String, byteArray: ByteArray? = null): Int? {
    return if (byteArray != null) {
        this.getExecutor(script, byteArray).executeInteger()
    } else {
        this.getExecutor(script).executeInteger()
    }
}

/**
 * 构建并执行脚本
 * 泛型为该方法从js中获取到的对象并返回，如果指定泛型则强制转换为该泛型对应的对象类型
 * @param script js script
 * @param byteArray cachedData – the cached data
 */
fun <T : Any> V8Runtime.executeObject(@Language("JavaScript") script: String, byteArray: ByteArray? = null): T? {
    return if (byteArray != null) {
        this.getExecutor(script, byteArray).executeObject()
    } else {
        this.getExecutor(script).executeObject()
    }
}

/**
 * 构建并执行脚本
 * @param script js script
 * @param byteArray cachedData – the cached data
 */
fun V8Runtime.executeZonedDateTime(
    @Language("JavaScript") script: String,
    byteArray: ByteArray? = null,
): ZonedDateTime? {
    return if (byteArray != null) {
        this.getExecutor(script, byteArray).executeZonedDateTime()
    } else {
        this.getExecutor(script).executeZonedDateTime()
    }
}

/**
 * 构建并执行脚本。Execute and return a primitive.
 * @param script js script
 * @param byteArray cachedData – the cached data
 */
fun <R, T : V8ValuePrimitive<R>?> V8Runtime.executePrimitive(
    @Language("JavaScript") script: String,
    byteArray: ByteArray? = null,
): R {
    return if (byteArray != null) {
        this.getExecutor(script, byteArray).executePrimitive<R, T>()
    } else {
        this.getExecutor(script).executePrimitive<R, T>()
    }
}

/**
 * 简单的遍历js 对象
 */
fun V8ValueObject.foreach(scope: (Int, V8Value, V8Value) -> Unit) {
    forEach<V8Value, V8Value, Throwable> { key, value, e ->
        scope(key, value, e)
    }
}

/**
 * 简单的遍历js 对象
 */
fun V8ValueObject.foreach(scope: (V8Value, V8Value) -> Unit) {
    forEach<V8Value, V8Value, Throwable> { index, key, value ->
        scope(key, value)
    }
}

/**
 * 终止当前引擎中的脚本运行执行。
 * 在给定的隔离中强制终止 JavaScript 执行的当前线程。
 * 此方法可由任何线程使用，即使该线程尚未通过 Locker 对象获取 V8 锁。
 */
fun V8Runtime.stop() {
    terminateExecution()
}
