package lib.module

@JsName("\$crypto")

external object Crypto {
    /**
     * 加密
     * @param message 未加密字符串
     * @param key 密钥，注意：AES等算法要求KEY的长度是16位的倍数。例如 new CryptoKey("password12345678");
     * @param type 加密类型
     */
    fun encrypt(message: String, key: CryptoKey, type: String): Array<Byte>

    /**
     * 解密
     * @param message 加密后二进制数据
     * @param key 密钥，注意：AES等算法要求KEY的长度是16位的倍数。例如 new CryptoKey("password12345678");
     * @param type 加密类型
     * @param output 输出类型 如： { output: 'string' }
     */
    fun decrypt(message: Array<Byte>, key: CryptoKey, type: String, output: OutputType): Any
}

external interface CryptoKey {
    var key: String
}

external interface OutputType {
    var output: String
}
