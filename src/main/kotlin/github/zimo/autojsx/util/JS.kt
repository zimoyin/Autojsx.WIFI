package github.zimo.autojsx.util

import com.caoccao.javet.enums.JSRuntimeType
import com.caoccao.javet.interception.logging.JavetStandardConsoleInterceptor
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.interop.V8Host
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.interop.converters.JavetProxyConverter
import com.caoccao.javet.interop.engine.IJavetEngine
import com.caoccao.javet.interop.engine.JavetEnginePool
import com.caoccao.javet.interop.executors.IV8Executor
import com.caoccao.javet.node.modules.NodeModuleModule
import com.caoccao.javet.values.V8Value
import com.caoccao.javet.values.primitive.V8ValuePrimitive
import org.intellij.lang.annotations.Language
import java.io.File
import java.io.IOException
import java.math.BigInteger
import java.time.ZonedDateTime

/**
 * JS 引擎池
 */
private val javetEnginePool by lazy {
    JavetEnginePool<V8Runtime>()
}

/**
 * 从引擎池获取一个指定类型的引擎
 */
fun getJavetEngine(type: JSRuntimeType = JSRuntimeType.V8): IJavetEngine<V8Runtime> {
    javetEnginePool.config.setJSRuntimeType(type); // 设置引擎为 V8 或者 NODE
    return javetEnginePool.engine
}

/**
 * 从引擎池获取一个 V8类型的引擎
 */
fun getV8(): IJavetEngine<V8Runtime> {
    return getJavetEngine(JSRuntimeType.V8)
}

/**
 * 从引擎池获取一个 Node类型的引擎
 */
fun getNode(): IJavetEngine<V8Runtime> {
    return getJavetEngine(JSRuntimeType.Node)
}

/**
 * 从引擎池获取一个 V8类型的引擎
 * @param scope 引擎执行回调
 */
fun V8WithPool(scope: (V8Runtime) -> Unit): IJavetEngine<V8Runtime> {
    return getJavetEngine(JSRuntimeType.V8).apply {
        scope(this.v8Runtime)
    }
}

/**
 * 从引擎池获取一个 Node类型的引擎
 * @param scope 引擎执行回调
 */
fun NodeJsWithPool(scope: (NodeRuntime) -> Unit): IJavetEngine<V8Runtime> {
    return getJavetEngine(JSRuntimeType.Node).apply {
        scope(this.v8Runtime as NodeRuntime)
    }
}

/**
 * 获取一个 V8类型的引擎，执行完毕代码后就释放该引擎，执行完毕代码后就释放该引擎。如果想要复用请参照 V8WithPool
 * @param scope 引擎执行回调
 */
fun V8(scope: (V8Runtime) -> Unit) {
    V8Host.getV8Instance().createV8Runtime<V8Runtime>().use { v8Runtime ->
        try {
            scope(v8Runtime)
        } catch (e: Exception) {
            throw e;
        }
        v8Runtime.lowMemoryNotification()
        v8Runtime
    }
}

/**
 * 获取一个 Node类型的引擎，执行完毕代码后就释放该引擎。如果想要复用请参照 NodeJsWithPool
 * @param scope 引擎执行回调
 */
fun NodeJs(scope: (NodeRuntime) -> Unit) {
    V8Host.getNodeInstance().createV8Runtime<V8Runtime>().use { v8Runtime ->
        try {
            scope(v8Runtime as NodeRuntime)
        } catch (e: Exception) {
            throw e;
        }
        v8Runtime.lowMemoryNotification()
    }
}

/**
 * 设置Node 引擎的模块路径
 */
fun NodeRuntime.setNodeModuleRootDirectory(path: String) {
    getNodeModule(NodeModuleModule::class.java).setRequireRootDirectory(path)
}

/**
 * 设置Node 引擎的模块路径
 */
fun NodeRuntime.setNodeModuleRootDirectory(path: File) {
    getNodeModule(NodeModuleModule::class.java).setRequireRootDirectory(File("./temp/node_moduies"))
}

/**
 * 开启从java对象到 js对象转换的自动转换器
 */
fun V8Runtime.openObjectConverter() {
    // 步骤 1: 创建一个JavetProxyConverter实例。
    val javetProxyConverter = JavetProxyConverter()
    // 步骤 2: 将V8Runtime的转换器设置为 JavetProxyConverter。
    setConverter(javetProxyConverter)
}

/**
 * 对象拦截器，将JAVA 对象 与 JS中的对象进行绑定，当任意一方修改后，其他一方都会修改，并且 js 被允许调用该JAVA对象的方法
 * @param interceptor 拦截器，注意该拦截器要使用 @V8Property 注解标注 属性的 get/set 方法，使用 @V8Function 注解标注要向JS暴露的方法
 */
fun V8Runtime.interception(interceptor: Any) {
    createV8ValueObject().use { v8ValueObject ->
        //声明这个对象
        globalObject["a"] = v8ValueObject
        //将这个对象与JAVA对象进行绑定
        v8ValueObject.bind(interceptor)
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
 * @Title 将类 注入到JS执行环境中
 * @Tip 注意：需要开启 openObjectConverter()
 * 并且 不能注入已经被new( 数组/map/list/其他数据类型 除外) 的对象,这可能需要拦截器进行工作
 * 对于如果你需要注入一个已经被 new 的对象，你需要保证他是( 数组/map/list/其他数据类型 ) 任意一个。
 * 如果不是就尽可能不去调用该对象的方法,因为可能因为方法参数传值不正确而导致发生异常
 *
 * 《如果传入的是Class则无需要注意，如果传入的是对象则'可能'需要拦截器辅助进行工作,如果是JSON 对象 则不推荐拦截器而是推荐(JsonNode) IJavetDirectProxyHandler<Exception>》
 * @ep
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
    }finally {
        console.closeConsole()
    }
}

/**
 * 开启该控制台,注意要手动关闭控制台，否则可能会造成内存泄漏
 * 通过 setInfo,setWarn,setError。。。 可以重定向输出流
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
    }finally {
        this.closeConsole()
    }
}


/**
 * 在当前JS 执行环境中加载 js 文件
 */
fun V8Runtime.loadJS(jsFile: File) {
    if (jsFile.exists() && jsFile.canRead()) {
        getExecutor(jsFile).executeVoid()
    } else {
        throw IOException("Unable to load JS file: $jsFile")
    }
}

/**
 * 在当前JS 执行环境中加载 js 文件
 */
fun V8Runtime.loadJS(jsFilePath: String) {
    val jsFile = File(jsFilePath)
    if (jsFile.exists() && jsFile.canRead()) {
        getExecutor(jsFile).executeVoid()
    } else {
        throw IOException("Unable to load JS file: $jsFile")
    }
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
 * @param script js script
 * @param byteArray cachedData – the cached data
 */
fun <T : V8Value> V8Runtime.execute(@Language("JavaScript") script: String, byteArray: ByteArray? = null): T? {
    return if (byteArray != null) {
        this.getExecutor(script, byteArray).execute<T>()
    } else {
        this.getExecutor(script).execute<T>()
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
 * @param script js script
 * @param byteArray cachedData – the cached data
 */
fun <T> V8Runtime.executeObject(@Language("JavaScript") script: String, byteArray: ByteArray? = null): T? {
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
