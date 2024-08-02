package lib.module

@JsName("auto")
external object Auto {

    /**
     * 等待某些条件满足。
     */
    fun waitFor()

    /**
     * 设置自动化模式。
     * @param mode 模式，可以是 'fast' 或 'normal'
     */
    fun setMode(mode: String /* 'fast' | 'normal' */)
}
