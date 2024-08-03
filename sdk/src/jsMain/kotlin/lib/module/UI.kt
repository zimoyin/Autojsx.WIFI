package lib.module

import lib.packages.android

/**
 *
 * @author : zimo
 * @date : 2024/07/30
 */

@JsName("ui")
external object UI {
    // `xml` can be of type `UILike` or any other type
    fun layout(xml: dynamic) // You might want to use `Any` if you're not sure of the exact type

    // `xml` can be of type `UILike` or any other type, and `parent` can be `View` or `null`
    fun inflate(xml: dynamic, parent: Any? = definedExternally)

    // `id` is a `String`, and `findView` returns `View`
    fun findView(id: String): Any

    // `finish` is a function with no parameters and no return value
    fun finish()

    // `view` is of type `View`
    fun setContentView(view: Any)

    // `callback` is a function with no parameters and no return value
    fun run(callback: () -> Unit)

    // `callback` is a function with no parameters and no return value, `delay` is optional
    fun post(callback: () -> Unit, delay: Int? = definedExternally)

    // `color` can be `dynamic` depending on how color is represented (e.g., string, number)
    fun statusBarColor(color: dynamic)

    // `view` is of type `View`, `menu` can be `dynamic`
    fun showPopupMenu(view: Any, menu: dynamic)

    @JsName("web")
    class WebView {

        @JsName("webViewClient")
        var webViewClient: dynamic

        @JsName("webChromeClient")
        var webChromeClient: dynamic

        @JsName("evaluateJavascript")
        fun evaluateJavascript(code: String, b: dynamic)

        @JsName("loadUrl")
        fun loadUrl(url: String)

        @JsName("jsBridge")
        val jsBridge: JsBridge
    }

    val web: WebView
}