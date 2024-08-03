package lib.module

@JsModule("\$autox")
@JsNonModule
external interface AutoX {

    /**
     * 注册一个监听函数
     * @param name
     * @param handler (data, callBack) =>{}  data->来自安卓调用  callBack->callBack("web回调数据");
     */
    fun <T> registerHandler(name: String, handler: T)

    /**
     * 调用安卓端
     * @param name
     * @param data web调用数据
     * @param handler 安卓回调 (data)=>{} data->内容
     */
    fun <T> callHandler(name: String, data: String, handler: T)
}

/**
 * 注意：在web与安卓端传递的数据只能是字符串，其他数据需自行使用JSON序列化
 * 在调用callHandler时传入了回调函数，但web端没有调用则会造成内存泄露。
 * jsBridge自动注入依赖于webViewClient，如设置了自定义webViewClient则需要在合适的时机（页面加载完成后）调用webview.injectionJsBridge()手动注入
 */
@JsModule("ui.web.jsBridge")
@JsNonModule
external interface JsBridge {

    /**
     * 注册一个监听函数
     * @param name
     * @param handler (data, callBack) =>{}  data->web调用安卓  callBack->callBack("回调web");
     * ((data: String, callBack: (data: String) -> Unit) -> Unit)
     */
    fun registerHandler(name: String, handler: (data: String, callBack: (data: String) -> Unit) -> Unit)

    /**
     * 调用安卓端
     * @param name
     * @param data 数据
     * @param handler 安卓回调 (data)=>{} data->web回调
     */
    fun callHandler(name: String, data: String, handler: (data: String) -> Unit)
}

fun JsBridge.registerHandlerByKotlin(name: String, handler: (data: JsBridgeData) -> Unit) {
    registerHandler(name) { data, callBack ->
        handler(JsBridgeData(data, callBack))
    }
}

data class JsBridgeData(
    val data: String,
    val reply: (data: String) -> Unit
)
