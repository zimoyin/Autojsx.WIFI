package lib.module

/**
 * RootAutomator是一个使用root权限来模拟触摸的对象，用它可以完成触摸与多点触摸，并且这些动作的执行没有延迟。
 *
 * 一个脚本中最好只存在一个RootAutomator，并且保证脚本结束退出他。
 */
external class RootAutomator {
    /**
     * 点击位置(x, y)。其中id是一个整数值，用于区分多点触摸，不同的id表示不同的"手指"。
     */
    fun tap(x: Number, y: Number, id: Number? = definedExternally): Unit

    /**
     * 模拟一次从(x1, y1)到(x2, y2)的时间为duration毫秒的滑动。
     */
    fun swipe(x1: Number, x2: Number, y1: Number, y2: Number, duration: Number? = definedExternally): Unit

    /**
     * 模拟按下位置(x, y)，时长为duration毫秒。
     */
    fun press(x: Number, y: Number, duration: Number, id: Number? = definedExternally): Unit

    /**
     * 模拟长按位置(x, y)。
     */
    fun longPress(x: Number, y: Number, duration: Number? = definedExternally, id: Number? = definedExternally): Unit

    /**
     * 模拟手指按下位置(x, y)。
     */
    fun touchDown(x: Number, y: Number, id: Number? = definedExternally): Unit

    /**
     * 模拟移动手指到位置(x, y)。
     */
    fun touchMove(x: Number, y: Number, id: Number? = definedExternally): Unit

    /**
     * 模拟手指弹起。
     */
    fun touchUp(id: Number? = definedExternally): Unit
}
