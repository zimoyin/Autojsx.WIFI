package lib.module

// Define the zips namespace with constants and functions
@JsName("zips")
external object Zips {
    /**
     * 压缩
     * @param type 压缩类型，请使用 zips.TYPE_ZIP 等 来确定。支持的类型：zip 7z bz2 bzip2 tbz2 tbz gz gzip tgz tar wim swm xz txz。
     * @param filePath 压缩文件路径(必须是完整路径)
     * @param dirPath 目录路径(必须是完整路径)
     * @param password 压缩密码
     * @return number 你可以通过  zip.REQUEST_SUCCESS 等来确定返回值信息代表类型
     */
    fun A(type: String, filePath: String, dirPath: String, password: String): Int

    /**
     * 解压
     * @param filePath 压缩文件路径(必须是完整路径)
     * @param dirPath 目录路径(必须是完整路径)
     * @param password 压缩密码
     * @return number 你可以通过 zip.REQUEST_SUCCESS 等 来确定返回值信息代表类型
     */
    fun X(filePath: String, dirPath: String, password: String): Int
}

object ZipType{
    // Constants for compression types
    const val TYPE_ZIP: String = "zip"
    const val TYPE_7z: String = "7z"
    const val TYPE_BZ2: String = "bz2"
    const val TYPE_BZIP2: String = "bzip2"
    const val TYPE_TBZ: String = "tbz"
    const val TYPE_GZ: String = "gz"
    const val TYPE_GZIP: String = "gzip"
    const val TYPE_TGZ: String = "tgz"
    const val TYPE_TAR: String = "tar"
    const val TYPE_WIM: String = "wim"
    const val TYPE_SWM: String = "swm"
    const val TYPE_XZ: String = "xz"
    const val TYPE_TXZ: String = "txz"

    // Constants for return codes
    const val REQUEST_SUCCESS: Int = 0
    const val REQUEST_ERROR_EXCEPTION_EXIT: Int = 1
    const val REQUEST_FATAL_ERROR_EXIT: Int = 2
    const val REQUEST_ERROR_SHALL: Int = 7
    const val REQUEST_ERROR_NO_MEMORY: Int = 8
    const val REQUEST_SUCCESS_USER_TERMINATION: Int = 255
}
