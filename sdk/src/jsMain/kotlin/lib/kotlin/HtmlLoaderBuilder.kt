package lib.kotlin

import lib.module.log
import lib.packages.android
import lib.packages.ex.android.WebChromeClientListener
import lib.packages.ex.android.WebViewClientListener
import lib.packages.java
import kotlin.js.json

class HtmlLoaderBuilder(private val index: String) {
    data class PageStartedResult(val view: android.webkit.WebView, val url: String?, val favicon: dynamic)
    data class PageFinishedResult(val view: android.webkit.WebView, val url: dynamic)
    data class PageErrorResult(
        val view: android.webkit.WebView,
        val request: android.webkit.WebResourceRequest,
        val error: android.webkit.WebResourceError
    )

    data class ConsoleMessageResult(
        val message: String,
        val sourceId: String,
        val lineNumber: Int,
        val messageLevel: String
    )

    /**
     * 回调结果
     * Web 调用 Autojs 端的方法产生的结果集
     * @param cmd 调用 Autojs 端中的方法的名称/ 需要在Autojs 中执行的 Js 命令
     * @param args 调用 Autojs 端中的方法的参数
     * @param result 调用 Autojs 端中的方法的结果
     * @param returnResult 将结果返回给浏览器后，负责处理该回调结果的方法，返回的数据。该数据不少必须有的
     */
    data class CallbackResult(
        val cmd: String,
        val callId: String,
        val args: dynamic,
        val result: String?,
        val returnResult: String?
    )

    private val pageStartedListenerList: MutableList<(data: PageStartedResult) -> Unit> = mutableListOf()
    private val pageFinishedListenerList: MutableList<(data: PageFinishedResult) -> Unit> = mutableListOf()
    private val pageErrorListenerList: MutableList<(data: PageErrorResult) -> Unit> = mutableListOf()
    private val consoleMessageListenerList: MutableList<(data: ConsoleMessageResult) -> Unit> = mutableListOf()
    private val callbackListenerList: MutableList<(data: CallbackResult) -> Unit> = mutableListOf()
    private val callbackResultListenerList: MutableList<(data: CallbackResult) -> Unit> = mutableListOf()

    fun onPageStarted(callback: (data: PageStartedResult) -> Unit): HtmlLoaderBuilder {
        pageStartedListenerList.add(callback)
        return this
    }

    fun onPageFinished(callback: (data: PageFinishedResult) -> Unit): HtmlLoaderBuilder {
        pageFinishedListenerList.add(callback)
        return this
    }

    fun onPageError(callback: (data: PageErrorResult) -> Unit): HtmlLoaderBuilder {
        pageErrorListenerList.add(callback)
        return this
    }

    fun onConsoleMessage(callback: (data: ConsoleMessageResult) -> Unit): HtmlLoaderBuilder {
        consoleMessageListenerList.add(callback)
        return this
    }

    fun onCallback(callback: (data: CallbackResult) -> Unit): HtmlLoaderBuilder {
        callbackListenerList.add(callback)
        return this
    }

    fun onCallbackResult(callback: (data: CallbackResult) -> Unit): HtmlLoaderBuilder {
        callbackResultListenerList.add(callback)
        return this
    }

    private fun buildWebViewClientListener(): WebViewClientListener {
        return object : WebViewClientListener {
            override fun onPageStarted(view: android.webkit.WebView, url: String?, favicon: dynamic) {
                pageStartedListenerList.forEach {
                    runCatching { it(PageStartedResult(view, url, favicon)) }.onFailure {
                        console.error("onPageStarted error: %s", it.message)
                    }
                }
            }

            override fun onPageFinished(view: android.webkit.WebView, url: dynamic) {
                pageFinishedListenerList.forEach {
                    runCatching { it(PageFinishedResult(view, url)) }.onFailure {
                        console.error("onPageFinished error: %s", it.message)
                    }
                }
            }

            override fun onReceivedError(
                view: android.webkit.WebView,
                request: android.webkit.WebResourceRequest,
                error: android.webkit.WebResourceError
            ) {
                pageErrorListenerList.forEach {
                    runCatching { it(PageErrorResult(view, request, error)) }.onFailure {
                        console.error("onPageError error: %s", it.message)
                    }
                }
            }

        }
    }

    private fun buildWebChromeClientListener(): WebChromeClientListener {
        return object : WebChromeClientListener {
            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage) {
                val msg = consoleMessage.message()
                val sourceId = consoleMessage.sourceId().split("/")
                val sourceIdStr = sourceId.last()
                val lineNumber = consoleMessage.lineNumber()
                val msgLevel = consoleMessage.messageLevel()

                if (msg.startsWith("jsbridge://")) {

                    val uris = msg.split("/")
                    val data = JSON.parse<dynamic>(java.net.URLDecoder.decode(uris[2], "UTF-8"))
                    val cmd = data.cmd
                    val callId = data.callId
                    val args = data.args

                    var result: dynamic = null

                    try {
                        result = HtmlLoader.executingLocalCode(cmd, args)
                    } catch (e: dynamic) {
                        console.error(e)
                        result = eval("{message: e.message}")
                    }
                    if (result == undefined) result = null

                    callbackListenerList.forEach {
                        runCatching { it(CallbackResult(cmd, callId, args, result, null)) }.onFailure {
                            console.error("onCallback error: %s", it.message)
                        }
                    }
                    val callbackArgs = json("callId" to callId, "args" to result).toJsonString()
                    HtmlLoader.callJs("auto.callback($callbackArgs)") {
                        callbackResultListenerList.forEach {
                            runCatching { it(CallbackResult(cmd, callId, args, result, "success")) }.onFailure {
                                console.error("onCallbackResult error: %s", it.message)
                            }
                        }
                    }
                } else {
                    consoleMessageListenerList.forEach {
                        runCatching { it(ConsoleMessageResult(msgLevel, sourceIdStr, lineNumber, msg)) }.onFailure {
                            console.error("onConsoleMessage error: %s", it.message)
                        }
                    }
                }
            }
        }
    }

    fun build(): HtmlLoader {
        return HtmlLoader(index, buildWebViewClientListener(), buildWebChromeClientListener())
    }
}