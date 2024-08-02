import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.kotlin.*
import lib.module.Thread
import lib.module.getName
import lib.module.open
import lib.module.sleep
import kotlin.js.json

/**
 * 插件：
 * 修复bug
 * 监听打包并根据配置文件进行相应操作（运行，上传等）
 * 运行脚本，拉起控制台
 */
fun main() {
    test0()
}


@JsName("test0")
fun test0() {
    println("Hello World!!")
}

// 协程 @see Async.kt
fun test1() {
    launch {
        for (i in 0 until 100) {
            println("[${Thread.getName()} 线程] [$name 协程] 计数 $i")
            delay(1000)
        }
    }
}

// 执行JS，原生调用JS函数
fun test3() {
    js("console.log('Hello World')")

    // 示例一
    js("console.log")("Hello World")

    // 示例二
    val function = js("console.log")
    function("Hello World")

    // eval 也可以执行js，并且允许参数为字符串变量

    // JS 可以访问 Kotlin 方法，这是来自于 Kotlin/Js 的互操作机制
    val test_fffff = test0()
    js("test_fffff()")

    // 如果想要访问全局属性内容需要使用 @JsName 注解进行标注
    eval("test0()")
    js("test0()")
}

// 加载 Js 脚本
fun test2() {
    loadJsFile("Test.js") // 加载并执行 js 文件
    require("Test.js") // 导入 js 文件
}

// 线程
fun test4() {
    Thread.start {
        for (i in 0 until 100) {
            println("线程计数 $i")
            sleep(1000)
        }

        // 注意如果在线程里面更新UI，请使用 ui{} 进行包裹，让其在UI线程中进行更新
        ui { }
    }
}

// 访问文件
fun test5() {
    val file = open("./Test.txt", "r")
    val text = file.read()
    println(text)
}

// 序列化
@Serializable
data class User(val name: String, val age: Int)
fun test6() {
    val user = User("Alice", 25)

    // 序列化
    val jsonString = Json.encodeToString(user)
    println("Serialized JSON: $jsonString")

    // 反序列化
    val deserializedUser = Json.decodeFromString<User>(jsonString)
    println("Deserialized User: $deserializedUser")

    //  buildJsonArray {  }  // 构建 JsonArray
    //  buildJsonObject {  } // 构建 JsonObject

    // Json.parseToJsonElement() // 解析 json
    // Json.parseToJsonElement().jsonObject
    // Json.parseToJsonElement().jsonArray
}

// JSON 使用 JS 库
fun test7() {
    val json = json(
        "name" to "Alice",
        "age" to 25
    )
//    JSON.parse(json.toJsonString()) // json 转为 js 对象的方法
//    JSON.stringify() // js 对象 转为 json 的方法

    // js 的 Json 对象可以通过 toXXX 方法转为  kotlin-Serializable-json 的对象
}