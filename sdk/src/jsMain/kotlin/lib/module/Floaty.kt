package lib.module

@JsName("floaty")

external object Floaty {

    /**
     * 创建一个悬浮窗
     * @param layout 布局
     * @return 返回一个悬浮窗对象
     */
    fun window(layout: Any): FloatyWindow

    /**
     * 关闭所有悬浮窗
     */
    fun closeAll(): Unit

    /**
     * 悬浮窗接口
     */
    interface FloatyWindow {
        /**
         * 设置是否允许调整悬浮窗大小
         * @param enabled 是否允许调整
         */
        fun setAdjustEnabled(enabled: Boolean): Unit

        /**
         * 设置悬浮窗位置
         * @param x X轴坐标
         * @param y Y轴坐标
         */
        fun setPosition(x: Int, y: Int): Unit

        /**
         * 获取悬浮窗的X轴坐标
         * @return X轴坐标
         */
        fun getX(): Int

        /**
         * 获取悬浮窗的Y轴坐标
         * @return Y轴坐标
         */
        fun getY(): Int

        /**
         * 设置悬浮窗大小
         * @param width 宽度
         * @param height 高度
         */
        fun setSize(width: Int, height: Int): Unit

        /**
         * 获取悬浮窗的宽度
         * @return 宽度
         */
        fun getWidth(): Int

        /**
         * 获取悬浮窗的高度
         * @return 高度
         */
        fun getHeight(): Int

        /**
         * 关闭悬浮窗
         */
        fun close(): Unit

        /**
         * 设置悬浮窗关闭时是否退出脚本
         */
        fun exitOnClose(): Unit
    }
}
