package lib.kotlin

import lib.module.Packages
import lib.module.importPackage

/**
 *
 * @author : zimo
 * @date : 2024/07/30
 */
class Websocket {
    init {
        importPackage(Packages["okhttp3"]); //导入包
    }

    fun createClient(url: String, listener: WebSocketListenerByKotlin): WebSocket {
        val OkHttpClient = js("OkHttpClient")
        val Request = js("Request")
        val WebSocketListener = js("WebSocketListener")

        var client = OkHttpClient.Builder().retryOnConnectionFailure(true).build()
        var request = Request.Builder().url(url).build();
        client.dispatcher().cancelAll();//清理一次

        var webSocket = client.newWebSocket(request, WebSocketListener(listener)); //创建链接

        return webSocket as WebSocket
    }
}

external interface WebSocket{
    fun send(msg: Any)
    fun close(code: Int, reason: String)
    fun close()
}

interface WebSocketListenerByKotlin {
    /**
     * 连接成功
     */
    @JsName("onOpen")
    fun onOpen(webSocket: WebSocket, response: Any)

    /**
     * 收到消息
     */
    @JsName("onMessage")
    fun onMessage(webSocket: WebSocket, msg: Any)

    /**
     * 连接关闭
     */
    @JsName("onClosing")
    fun onClosing(webSocket: WebSocket, code: Int, reason: String)

    /**
     * 连接关闭
     */
    @JsName("onClosed")
    fun onClosed(webSocket: WebSocket, code: Int, reason: String)

    /**
     * 连接失败
     */
    @JsName("onFailure")
    fun onFailure(webSocket: WebSocket, t: Throwable, response: Any?)
}