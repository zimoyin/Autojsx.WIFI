package lib.packages

/**
 *
 * @author : zimo
 * @date : 2024/08/03
 */
@JsName("java")
external class java {

    @JsName("net")
    class net {

        @JsName("URLDecoder")
        class URLDecoder {
            companion object {
                @JsName("decode")
                fun decode(str: String, charset: String): String
            }
        }

        @JsName("URLEncoder")
        class URLEncoder {
            companion object {
                @JsName("encode")
                fun encode(str: String, charset: String): String
            }
        }

        @JsName("URL")
        class URL(url: String) {
            constructor(protocol: String, host: String, port: Int, file: String)
            constructor(protocol: String, host: String, file: String)
            constructor(url: URL, spec: String)

            @JsName("isValidProtocol")
            fun isValidProtocol(protocol: String): Boolean

            @JsName("set")
            fun set(protocol: String, host: String, port: Int, file: String, authority: Boolean)

            @JsName("getHostAddress")
            fun getHostAddress(): String

            @JsName("getQuery")
            fun getQuery(): String?

            @JsName("getPath")
            fun getPath(): String

            @JsName("getUserInfo")
            fun getUserInfo(): String?

            @JsName("getAuthority")
            fun getAuthority(): String

            @JsName("getPort")
            fun getPort(): Int

            @JsName("getDefaultPort")
            fun getDefaultPort(): Int

            @JsName("getProtocol")
            fun getProtocol(): String

            @JsName("getHost")
            fun getHost(): String

            @JsName("getFile")
            fun getFile(): String

            @JsName("getRef")
            fun getRef(): String?

            @JsName("sameFile")
            fun sameFile(other: URL): Boolean

            @JsName("toExternalForm")
            fun toExternalForm(): String

            @JsName("toURI")
            fun toURI(): URI

            @JsName("openConnection")
            fun openConnection(): URLConnection

            @JsName("openStream")
            fun openStream(): io.InputStream

            @JsName("getContent")
            fun getContent(): Any?


            companion object {
                @JsName("fromURI")
                fun fromURI(uri: URI): URL
            }
        }

        @JsName("URI")
        class URI {
            @JsName("getPort")
            fun getPort(): Int

            @JsName("getHost")
            fun getHost(): String?

            @JsName("getPath")
            fun getPath(): String?

            @JsName("getRawPath")
            fun getRawPath(): String?

            @JsName("getQuery")
            fun getQuery(): String?

            @JsName("getRawQuery")
            fun getRawQuery(): String?

            @JsName("getFragment")
            fun getFragment(): String?

            @JsName("getRawFragment")
            fun getRawFragment(): String?

            @JsName("isOpaque")
            fun isOpaque(): Boolean

            @JsName("isAbsolute")
            fun isAbsolute(): Boolean

            @JsName("getScheme")
            fun getScheme(): String?

            @JsName("toURL")
            fun toURL(): URL

            @JsName("relativize")
            fun relativize(uri: URI): URI

            @JsName("resolve")
            fun resolve(uri: URI): URI

            @JsName("getSchemeSpecificPart")
            fun getSchemeSpecificPart(): String

            @JsName("getRawAuthority")
            fun getRawAuthority(): String?

            @JsName("getAuthority")
            fun getAuthority(): String?

            @JsName("getRawUserInfo")
            fun getRawUserInfo(): String?

            @JsName("getUserInfo")
            fun getUserInfo(): String?

            @JsName("getRawSchemeSpecificPart")
            fun getRawSchemeSpecificPart(): String

            @JsName("toASCIIString")
            fun toASCIIString(): String

            companion object {
                @JsName("create")
                fun create(str: String): URI

                @JsName("parseServerAuthority")
                fun parseServerAuthority(uri: URI): URI
            }
        }

        @JsName("URLConnection")
        class URLConnection {
            @JsName("getRequestProperties")
            fun getRequestProperties(): Map<String, List<String>>

            @JsName("setDoOutput")
            fun setDoOutput(doOutput: Boolean)

            @JsName("getContentEncoding")
            fun getContentEncoding(): String?

            @JsName("getContentLengthLong")
            fun getContentLengthLong(): Long

            @JsName("getHeaderFields")
            fun getHeaderFields(): Map<String, List<String>>

            @JsName("getAllowUserInteraction")
            fun getAllowUserInteraction(): Boolean

            @JsName("setDefaultUseCaches")
            fun setDefaultUseCaches(defaultUseCaches: Boolean)

            @JsName("getHeaderFieldInt")
            fun getHeaderFieldInt(name: String, default: Int): Int

            @JsName("getConnectTimeout")
            fun getConnectTimeout(): Int

            @JsName("getDefaultUseCaches")
            fun getDefaultUseCaches(): Boolean

            @JsName("getHeaderField")
            fun getHeaderField(name: String): String?

            @JsName("getDoInput")
            fun getDoInput(): Boolean

            @JsName("setConnectTimeout")
            fun setConnectTimeout(timeout: Int)

            @JsName("getUseCaches")
            fun getUseCaches(): Boolean

            @JsName("connect")
            fun connect()

            @JsName("getDoOutput")
            fun getDoOutput(): Boolean

            @JsName("setDoInput")
            fun setDoInput(doInput: Boolean)

            @JsName("getContentLength")
            fun getContentLength(): Int

            @JsName("setAllowUserInteraction")
            fun setAllowUserInteraction(allowUserInteraction: Boolean)

            @JsName("getLastModified")
            fun getLastModified(): Long

            @JsName("getHeaderFieldLong")
            fun getHeaderFieldLong(name: String, default: Long): Long

            @JsName("getRequestProperty")
            fun getRequestProperty(key: String): String?

            @JsName("getOutputStream")
            fun getOutputStream(): io.OutputStream

            @JsName("setReadTimeout")
            fun setReadTimeout(timeout: Int)

            @JsName("getHeaderFieldKey")
            fun getHeaderFieldKey(n: Int): String?

            @JsName("setUseCaches")
            fun setUseCaches(useCaches: Boolean)

            @JsName("getContent")
            fun getContent(): Any?

            @JsName("getExpiration")
            fun getExpiration(): Long

            @JsName("addRequestProperty")
            fun addRequestProperty(key: String, value: String)

            @JsName("getDate")
            fun getDate(): Long

            @JsName("getIfModifiedSince")
            fun getIfModifiedSince(): Long

            @JsName("getURL")
            fun getURL(): URL

            @JsName("setRequestProperty")
            fun setRequestProperty(key: String, value: String)

            @JsName("getInputStream")
            fun getInputStream(): io.InputStream

            @JsName("getReadTimeout")
            fun getReadTimeout(): Int

            @JsName("getHeaderFieldDate")
            fun getHeaderFieldDate(name: String, default: Long): Long

            @JsName("setIfModifiedSince")
            fun setIfModifiedSince(ifModifiedSince: Long)

            @JsName("getContentType")
            fun getContentType(): String?

            @JsName("setDefaultAllowUserInteraction")
            fun setDefaultAllowUserInteraction(defaultAllowUserInteraction: Boolean)

            @JsName("getDefaultAllowUserInteraction")
            fun getDefaultAllowUserInteraction(): Boolean
        }

    }


    @JsName("io")
    class io {

        @JsName("InputStream")
        abstract class InputStream {
            @JsName("read")
            fun read(): Int

            @JsName("readBytes")
            fun read(b: ByteArray): Int

            @JsName("readBytesWithOffset")
            fun read(b: ByteArray, off: Int, len: Int): Int

            @JsName("readAllBytes")
            fun readAllBytes(): ByteArray

            @JsName("readNBytes")
            fun readNBytes(len: Int): ByteArray

            @JsName("readNBytesWithBuffer")
            fun readNBytes(b: ByteArray, off: Int, len: Int): Int

            @JsName("skip")
            fun skip(n: Long): Long

            @JsName("skipNBytes")
            fun skipNBytes(n: Long)

            @JsName("available")
            fun available(): Int

            @JsName("close")
            fun close()

            @JsName("mark")
            fun mark(readlimit: Int)

            @JsName("reset")
            fun reset()

            @JsName("markSupported")
            fun markSupported(): Boolean

            @JsName("transferTo")
            fun transferTo(out: OutputStream): Long
        }

        @JsName("OutputStream")
        abstract class OutputStream {
            @JsName("write")
            fun write(b: Int)

            @JsName("writeBytes")
            fun write(b: ByteArray)

            @JsName("writeBytesWithOffset")
            fun write(b: ByteArray, off: Int, len: Int)

            @JsName("flush")
            fun flush()

            @JsName("close")
            fun close()
        }

        @JsName("FileInputStream")
        class FileInputStream(path: String) : InputStream

        @JsName("FileOutputStream")
        class FileOutputStream(path: String) : OutputStream
    }
}