package lib.module

@JsName("\$base64")

external object Base64 {
    /**
     * 编码
     * @param str 要编码的字符串
     * @return 编码后的字符串
     */
    fun encode(str: String): String

    /**
     * 解码
     * @param str 要解码的字符串
     * @return 解码后的字符串
     */
    fun decode(str: String): String
}
