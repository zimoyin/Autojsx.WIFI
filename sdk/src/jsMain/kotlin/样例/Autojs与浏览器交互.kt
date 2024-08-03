package 样例

import lib.kotlin.HtmlLoader
import lib.module.log
import test0

/**
 *
 * @author : zimo
 * @date : 2024/08/03
 */
fun main() {
    /**
     * 可以通过 HtmlLoader.create("web/index.html").onXXX 来设置各种监听。监听必须在 build 之前创建
     */
    HtmlLoader.create("web/index.html").build().start()
}

@JsName("ajFun0")
fun ajFun0() {
    log("ajFun0 被 web 调用")
}

@JsName("ajFun1")
fun ajFun1(): Int {
    log("ajFun2 被 web 调用")
    return 1
}

@JsName("ajFun2")
fun ajFun2(name: String): Boolean {
    log("ajFun2 被 web 调用: name-> %s", name)
    return false
}

@JsName("ajFun3")
fun ajFun3(): String {
    return "我是字符串"
}

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