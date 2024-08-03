package lib.packages.ex.android

import lib.packages.android

external interface WebViewClientListener {
    /**
     * 页面开始加载, 此时还没有加载 index.html 中的代码
     */
    @JsName("onPageStarted")
    fun onPageStarted(view: android.webkit.WebView, url: String?, favicon: android.graphics.Bitmap?)

    /**
     * 页面加载完成, 在 window.onload 之后触发
     */
    @JsName("onPageFinished")
    fun onPageFinished(view: android.webkit.WebView, url: dynamic)

    @JsName("onReceivedError")
    fun onReceivedError(
        view: android.webkit.WebView,
        request: android.webkit.WebResourceRequest,
        error: android.webkit.WebResourceError
    )
}

external interface WebChromeClientListener {
    @JsName("onConsoleMessage")
    fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage): Unit
//    fun onConsoleMessage(consoleMessage: dynamic): Unit
}

external interface ValueCallbackListener {
    @JsName("onReceiveValue")
    fun onReceiveValue(value: dynamic)
}