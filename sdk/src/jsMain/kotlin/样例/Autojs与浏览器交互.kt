package 样例
import lib.kotlin.HtmlLoader
import lib.module.Thread
import lib.module.getName
import lib.module.log

/**
 *
 * @author : zimo
 * @date : 2024/08/03
 */
fun main() {
    /**
     * 可以通过 HtmlLoader.create("web/index.html").onXXX 来设置各种监听。监听必须在 build 之前创建
     */
    val htmlLoaderBuilder = HtmlLoader.create("web/web/index.html")
    // 浏览器加载页面完成
    htmlLoaderBuilder.onPageFinished {
        // 在浏览器执行JS
        HtmlLoader.callJs("console.log('Hello World')")
        println("页面加载完成: ${it.url}")
    }
    // 浏览器开始加载页面
    htmlLoaderBuilder.onPageStarted {
        println("页面开始加载: ${it.url}")
    }
    // 浏览器加载页面错误
    htmlLoaderBuilder.onPageError {
        println("页面加载错误: ${it.error}")
    }
    // 浏览器回调结果
    htmlLoaderBuilder.onCallbackResult {
        println("浏览器调用AUTOJS  回调结果: ${it.cmd} -> ${it.result}")
    }
    // 浏览器回调
    htmlLoaderBuilder.onCallback {
        println("浏览器调用AUTOJS  回调: ${it.cmd}-> ${it.result}")
    }
    // 浏览器控制台输出
    htmlLoaderBuilder.onConsoleMessage {
        println("[${Thread.getName()} 线程][浏览器]: ${it.message}")
    }
    // WebView 的 WebSettings  配置： 可见文章 https://www.jianshu.com/p/90823049a389
    htmlLoaderBuilder.config.apply {
        this.saveFormData = true
    }
    val htmlLoader = htmlLoaderBuilder.build()
    htmlLoader.start()
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("ajFun0")
fun ajFun0() {
    log("ajFun0 被 web 调用")
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("ajFun1")
fun ajFun1(): Int {
    log("ajFun2 被 web 调用")
    return 1
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("ajFun2")
fun ajFun2(name: String): Boolean {
    log("ajFun2 被 web 调用: name-> %s", name)
    return false
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("ajFun3")
fun ajFun3(): String {
    return "我是字符串"
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("ajFun4")
fun ajFun4(index: Int, name: String, age: Int): List<Map<String, Any>> {
    log("ajFun4 被 web 调用: index-> %s, name-> %s, age-> %s", index, name, age)

    //测试 aj调用web
    HtmlLoader.callJs("testAJ2Web(['返回值1', '返回值2', '返回值3'])") { data ->
        log(" AJ 接收到 testAJ2Web 的返回值 :", data)
    }

    val data = eval("""
        [
            { id: 1, name: "小明1", address: "北京1" },
            { id: 2, name: "小明2", address: "北京2" },
            { id: 3, name: "小明3", address: "北京3" },
        ]
    """.trimIndent())

    return data
}