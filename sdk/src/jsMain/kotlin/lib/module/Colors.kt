package lib.module

@JsName("colors")

external object Colors {

    /**
     * 将颜色值转换为字符串表示
     * @param color 颜色值
     * @return 颜色的字符串表示
     */
    fun toString(color: Number): String

    /**
     * 获取颜色的红色分量
     * @param color 颜色值或颜色字符串
     * @return 红色分量
     */
    fun red(color: dynamic): Number

    /**
     * 获取颜色的绿色分量
     * @param color 颜色值或颜色字符串
     * @return 绿色分量
     */
    fun green(color: dynamic): Number

    /**
     * 获取颜色的蓝色分量
     * @param color 颜色值或颜色字符串
     * @return 蓝色分量
     */
    fun blue(color: dynamic): Number

    /**
     * 获取颜色的透明度分量
     * @param color 颜色值或颜色字符串
     * @return 透明度分量
     */
    fun alpha(color: dynamic): Number

    /**
     * 创建 RGB 颜色值
     * @param red 红色分量
     * @param green 绿色分量
     * @param blue 蓝色分量
     * @return 颜色值
     */
    fun rgb(red: Number, green: Number, blue: Number): Number

    /**
     * 创建 ARGB 颜色值
     * @param alpha 透明度分量
     * @param red 红色分量
     * @param green 绿色分量
     * @param blue 蓝色分量
     * @return 颜色值
     */
    fun argb(alpha: Number, red: Number, green: Number, blue: Number): Number

    /**
     * 从字符串解析颜色值
     * @param colorStr 颜色字符串
     * @return 颜色值
     */
    fun parseColor(colorStr: String): Number

    /**
     * 判断两个颜色是否相似
     * @param color1 颜色值或颜色字符串
     * @param color2 颜色值或颜色字符串
     * @param threshold 相似度阈值
     * @param algorithm 相似度算法
     * @return 是否相似
     */
    fun isSimilar(color1: dynamic, color2: dynamic, threshold: Number, algorithm: String): Boolean

    /**
     * 判断两个颜色是否相等
     * @param color1 颜色值或颜色字符串
     * @param color2 颜色值或颜色字符串
     * @return 是否相等
     */
    fun equals(color1: dynamic, color2: dynamic): Boolean
}
