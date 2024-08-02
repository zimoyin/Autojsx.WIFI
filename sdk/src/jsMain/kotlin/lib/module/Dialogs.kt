package lib.module

import kotlin.js.Promise

/**
 * dialogs 模块提供了简单的对话框支持，可以通过对话框和用户进行交互。
 */
@JsName("dialogs")

external object Dialogs {

    /**
     * 显示一个只包含“确定”按钮的提示对话框。直至用户点击确定脚本才继续运行。
     */
    fun alert(title: String, content: String? = definedExternally): Unit

    /**
     * UI模式
     *
     * 显示一个只包含“确定”按钮的提示对话框。直至用户点击确定脚本才继续运行。
     */
    fun alert(title: String, content: String? = definedExternally, callback: (() -> Unit)? = definedExternally): Promise<Unit>

    /**
     * 显示一个包含“确定”和“取消”按钮的提示对话框。如果用户点击“确定”则返回 true ，否则返回 false 。
     */
    fun confirm(title: String, content: String? = definedExternally): Boolean

    /**
     * UI模式
     *
     * 显示一个包含“确定”和“取消”按钮的提示对话框。如果用户点击“确定”则返回 true ，否则返回 false 。
     */
    fun confirm(title: String, content: String? = definedExternally, callback: ((Boolean) -> Unit)? = definedExternally): Promise<Boolean>

    /**
     * 显示一个包含输入框的对话框，等待用户输入内容，并在用户点击确定时将输入的字符串返回。如果用户取消了输入，返回null。
     */
    fun rawInput(title: String, prefill: String? = definedExternally): String?

    /**
     * UI模式
     *
     * 显示一个包含输入框的对话框，等待用户输入内容，并在用户点击确定时将输入的字符串返回。如果用户取消了输入，返回null。
     */
    fun rawInput(title: String, prefill: String? = definedExternally, callback: ((String?) -> Unit)? = definedExternally): Promise<String?>

    /**
     * 等效于 eval(dialogs.rawInput(title, prefill, callback)), 该函数和rawInput的区别在于，会把输入的字符串用eval计算一遍再返回，返回的可能不是字符串。
     */
    fun input(title: String, prefill: String? = definedExternally): Any?

    /**
     * UI模式
     *
     * 等效于 eval(dialogs.rawInput(title, prefill, callback)), 该函数和rawInput的区别在于，会把输入的字符串用eval计算一遍再返回，返回的可能不是字符串。
     */
    fun input(title: String, prefill: String? = definedExternally, callback: ((Any?) -> Unit)? = definedExternally): Promise<Any?>

    /**
     * 显示一个包含输入框的对话框，等待用户输入内容，并在用户点击确定时将输入的字符串返回。如果用户取消了输入，返回null。
     */
    fun prompt(title: String, prefill: String? = definedExternally): String?

    /**
     * UI模式
     *
     * 显示一个包含输入框的对话框，等待用户输入内容，并在用户点击确定时将输入的字符串返回。如果用户取消了输入，返回null。
     */
    fun prompt(title: String, prefill: String? = definedExternally, callback: ((String?) -> Unit)? = definedExternally): Promise<String?>

    /**
     * 显示一个带有选项列表的对话框，等待用户选择，返回用户选择的选项索引(0 ~ item.length - 1)。如果用户取消了选择，返回-1。
     */
    fun select(title: String, items: Array<String>): Int

    /**
     * UI模式
     *
     * 显示一个带有选项列表的对话框，等待用户选择，返回用户选择的选项索引(0 ~ item.length - 1)。如果用户取消了选择，返回-1。
     */
    fun select(title: String, items: Array<String>, callback: ((Int) -> Unit)? = definedExternally): Promise<Int>

    /**
     * 显示一个单选列表对话框，等待用户选择，返回用户选择的选项索引(0 ~ item.length - 1)。如果用户取消了选择，返回-1。
     */
    fun singleChoice(title: String, items: Array<String>, index: Int? = definedExternally): Int

    /**
     * UI模式
     *
     * 显示一个单选列表对话框，等待用户选择，返回用户选择的选项索引(0 ~ item.length - 1)。如果用户取消了选择，返回-1。
     */
    fun singleChoice(title: String, items: Array<String>, index: Int? = definedExternally, callback: ((Int) -> Unit)? = definedExternally): Promise<Int>

    /**
     * 显示一个多选列表对话框，等待用户选择，返回用户选择的选项索引的数组。如果用户取消了选择，返回[]。
     */
    fun multiChoice(title: String, items: Array<String>, indices: Array<Int>? = definedExternally): Array<Int>

    /**
     * UI模式
     *
     * 显示一个多选列表对话框，等待用户选择，返回用户选择的选项索引的数组。如果用户取消了选择，返回[]。
     */
    fun multiChoice(title: String, items: Array<String>, indices: Array<Int>? = definedExternally, callback: ((Array<Int>) -> Unit)? = definedExternally): Promise<Array<Int>>

}
