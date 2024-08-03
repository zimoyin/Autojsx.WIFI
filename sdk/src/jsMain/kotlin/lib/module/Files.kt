package lib.module

@JsName("files")

external object Files {
//    typealias Byte = Number

    /**
     * 检查指定路径是否为文件
     * @param path 文件路径
     * @return 是否为文件
     */
    fun isFile(path: String): Boolean

    /**
     * 检查指定路径是否为目录
     * @param path 目录路径
     * @return 是否为目录
     */
    fun isDir(path: String): Boolean

    /**
     * 检查指定路径是否为空目录
     * @param path 目录路径
     * @return 是否为空目录
     */
    fun isEmptyDir(path: String): Boolean

    /**
     * 将多个路径组合成一个路径
     * @param parent 父路径
     * @param child 子路径
     * @return 组合后的路径
     */
    fun join(parent: String, vararg child: String): String

    /**
     * 创建文件
     * @param path 文件路径
     * @return 是否创建成功
     */
    fun create(path: String): Boolean

    /**
     * 创建文件及其父目录
     * @param path 文件路径
     * @return 是否创建成功
     */
    fun createWithDirs(path: String): Boolean

    /**
     * 检查文件或目录是否存在
     * @param path 文件或目录路径
     * @return 是否存在
     */
    fun exists(path: String): Boolean

    /**
     * 确保目录存在，不存在则创建
     * @param path 目录路径
     */
    fun ensureDir(path: String): Unit

    /**
     * 读取文件内容
     * @param path 文件路径
     * @param encoding 编码
     * @return 文件内容
     */
    fun read(path: String, encoding: String? = definedExternally): String

    /**
     * 读取文件字节内容
     * @param path 文件路径
     * @return 文件字节内容
     */
    fun readBytes(path: String): ByteArray

    /**
     * 写入文件内容
     * @param path 文件路径
     * @param text 文件内容
     * @param encoding 编码
     */
    fun write(path: String, text: String, encoding: String? = definedExternally): Unit

    /**
     * 写入文件字节内容
     * @param path 文件路径
     * @param bytes 文件字节内容
     */
    fun writeBytes(path: String, bytes: ByteArray): Unit

    /**
     * 追加文件内容
     * @param path 文件路径
     * @param text 追加的内容
     * @param encoding 编码
     */
    fun append(path: String, text: String, encoding: String? = definedExternally): Unit

    /**
     * 追加文件字节内容
     * @param path 文件路径
     * @param bytes 追加的字节内容
     * @param encoding 编码
     */
    fun appendBytes(path: String, bytes: ByteArray, encoding: String? = definedExternally): Unit

    /**
     * 复制文件
     * @param frompath 源文件路径
     * @param topath 目标文件路径
     * @return 是否复制成功
     */
    fun copy(frompath: String, topath: String): Boolean

    /**
     * 移动文件
     * @param frompath 源文件路径
     * @param topath 目标文件路径
     * @return 是否移动成功
     */
    fun move(frompath: String, topath: String): Boolean

    /**
     * 重命名文件
     * @param path 文件路径
     * @param newName 新名称
     * @return 是否重命名成功
     */
    fun rename(path: String, newName: String): Boolean

    /**
     * 重命名文件但不改变扩展名
     * @param path 文件路径
     * @param newName 新名称
     * @return 是否重命名成功
     */
    fun renameWithoutExtension(path: String, newName: String): Boolean

    /**
     * 获取文件名称
     * @param path 文件路径
     * @return 文件名称
     */
    fun getName(path: String): String

    /**
     * 获取没有扩展名的文件名称
     * @param path 文件路径
     * @return 没有扩展名的文件名称
     */
    fun getNameWithoutExtension(path: String): String

    /**
     * 获取文件扩展名
     * @param path 文件路径
     * @return 文件扩展名
     */
    fun getExtension(path: String): String

    /**
     * 删除文件
     * @param path 文件路径
     * @return 是否删除成功
     */
    fun remove(path: String): Boolean

    /**
     * 删除目录
     * @param path 目录路径
     * @return 是否删除成功
     */
    fun removeDir(path: String): Boolean

    /**
     * 获取SD卡路径
     * @return SD卡路径
     */
    fun getSdcardPath(): String

    /**
     * 获取当前工作目录
     * @return 当前工作目录
     */
    fun cwd(): String

    /**
     * 获取相对路径的绝对路径
     * @param relativePath 相对路径
     * @return 绝对路径
     */
    fun path(relativePath: String): String

    /**
     * 列出目录中的文件
     * @param path 目录路径
     * @param filter 过滤器函数
     * @return 符合条件的文件列表
     */
    fun listDir(path: String, filter: ((filename: String) -> Boolean)? = definedExternally): Array<String>

    /**
     *
     * @mode {string} 文件打开模式，包括:
     *      r: 只读文本模式。该模式下只能对文件执行文本读取操作。
     *      w: 只写文本模式。该模式下只能对文件执行文本覆盖写入操作。
     *      a: 附加文本模式。该模式下将会把写入的文本附加到文件末尾。
     *      rw: 随机读写文本模式。该模式下将会把写入的文本附加到文件末尾。
     *      目前暂不支持二进制模式，随机读写模式。
     * @encoding {string} 字符编码。
     * @bufferSize {number} 文件读写的缓冲区大小。
     */
    fun open(
        path: String,
        mode: String /* 'r' */,
        encoding: String? = definedExternally,
        bufferSize: Int? = definedExternally
    ): PReadableTextFile
}
