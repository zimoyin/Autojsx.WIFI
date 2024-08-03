package lib.kotlin

import lib.module.Files
import lib.module.PReadableTextFile
import lib.module.ReadableTextFile
import lib.module.WritableTextFile

/**
 *
 * @author : zimo
 * @date : 2024/08/02
 */
class File(private val path0: String, private val child0: String? = null) {

    val path: String
        get() {
            val isEndWithSlash = path0.trim().endsWith("/") || path0.trim().endsWith("\\")
            val child = child0?.trim()?.let {
                if (it.endsWith("/") || it.endsWith("\\")) {
                    it.substring(0, it.length - 1)
                } else {
                    it
                }
            }
            return if (child == null) {
                if (isDirectoryEmpty) path0.substring(0, path0.length - 1) else path0
            } else {
                if (isEndWithSlash) {
                    path0.trim() + child
                } else {
                    path0.trim() + "/" + child
                }
            }
        }

    val isFile: Boolean
        get() = Files.isFile(path)

    val isDirectory: Boolean
        get() = Files.isDir(path)

    val isDirectoryEmpty: Boolean
        get() = Files.isEmptyDir(path)

    val absolutePath: String
        get() = Files.path(path)

    fun join(child: String): String {
        return Files.join(path, child.trim())
    }

    fun resolve(child: String): File {
        return File(path, child.trim())
    }

    /**
     * 创建一个文件或文件夹并返回是否创建成功。如果文件已经存在，则直接返回false。
     */
    @Deprecated("Use createFile or createDirectory instead")
    fun create(name: String): Boolean {
        return Files.create(name.trim())
    }

    fun createFile(name: String): Boolean {
        return Files.create(resolve(name).path)
    }

    fun createDirectory(name: String): Boolean {
        return Files.create(resolve(name).path + "/")
    }

    /**
     * 创建一个文件或文件夹并返回是否创建成功。如果文件所在文件夹不存在，则先创建他所在的一系列文件夹。如果文件已经存在，则直接返回false。
     */
    fun createWithDirs(name: String): Boolean {
        return Files.createWithDirs(name.trim())
    }

    fun mkdir(): Boolean {
        return Files.create("$path/")
    }

    fun mkdirs(): Boolean {
        return Files.createWithDirs("$path/")
    }

    fun exists(): Boolean {
        return Files.exists(path)
    }

    /**
     * 确保路径path所在的文件夹存在。如果该路径所在文件夹不存在，则创建该文件夹。
     */
    fun ensureDir(path: String) {
        if (!Files.exists(path)) {
            Files.createWithDirs(path)
        }
    }

    fun readText(encoding: String = "UTF-8"): String {
        return Files.read(path, encoding)
    }

    fun readBytes(): ByteArray {
        return Files.readBytes(path)
    }

    fun writeText(text: String, encoding: String = "UTF-8") {
        Files.write(path, text, encoding)
    }

    fun writeBytes(bytes: ByteArray) {
        Files.writeBytes(path, bytes)
    }

    fun writeAppendText(text: String, encoding: String = "UTF-8") {
        Files.append(path, text, encoding)
    }

    fun writeAppendBytes(bytes: ByteArray) {
        Files.appendBytes(path, bytes)
    }

    fun copy(toPath: String) {
        Files.copy(path, toPath)
    }

    fun move(toPath: String) {
        Files.move(path, toPath)
    }

    fun rename(newName: String) {
        Files.rename(path, newName)
    }

    fun renameWithoutExtension(newName: String) {
        Files.renameWithoutExtension(path, newName)
    }

    fun getName(): String {
        return Files.getName(path)
    }

    fun getNameWithoutExtension(): String {
        return Files.getNameWithoutExtension(path)
    }

    fun getExtension(): String {
        return Files.getExtension(path)
    }

    fun remove() {
        if (isFile) {
            Files.remove(path)
        } else {
            Files.removeDir(path)
        }
    }

    fun removeFile() {
        Files.remove(path)
    }

    fun removeDirectory() {
        Files.removeDir(path)
    }

    fun getSdcardPath(): String {
        return Files.getSdcardPath()
    }

    fun cwd(): String {
        return Files.cwd()
    }

    fun list(filter: ((filename: String) -> Boolean)? = null): Array<String> {
        return if (filter == null) {
            Files.listDir(path)
        } else {
            Files.listDir(path, filter)
        }
    }

    fun listFiles(filter: ((filename: String) -> Boolean)? = null): Array<File> {
        return if (filter == null) {
            list().map { File(path, it) }.toTypedArray()
        } else {
            list(filter).map { File(path, it) }.toTypedArray()
        }
    }

    fun parent(): String {
        return File(path.substring(0, path.lastIndexOf("/"))).path
    }

    fun parentFile(): File {
        return File(path.substring(0, path.lastIndexOf("/")))
    }

    fun findFile(name: String): File? {
        return list().find { it == name }?.let { File(path, it) }?.let { if (it.isFile) it else null }
    }

    fun findDirectory(name: String): File? {
        return list().find { it == name }?.let { File(path, it) }?.let { if (it.isDirectory) it else null }
    }

    fun findFileOrCreate(name: String): File {
        return findFile(name) ?: if (createFile(name)) {
            File(path, name)
        } else {
            throw Exception("Failed to create file $name")
        }
    }

    fun findDirectoryOrCreate(name: String): File {
        return findDirectory(name) ?: if (createDirectory(name)) {
            File(path, name)
        } else {
            throw Exception("Failed to create directory $name")
        }
    }

    fun delete() {
        remove()
    }

    fun open(
        path: String,
        mode: Mode = Mode.Read,
        encoding: String = "utf-8",
        bufferSize: Int = 8192
    ): PReadableTextFile {
        when (mode) {
            Mode.Read -> {
                return Files.open(path, mode.value, encoding, bufferSize) as ReadableTextFile
            }

            Mode.Write -> {
                return Files.open(path, mode.value, encoding, bufferSize) as WritableTextFile
            }

            Mode.Append -> {
                return Files.open(path, mode.value, encoding, bufferSize) as ReadableTextFile
            }

            Mode.ReadWrite -> {
                return Files.open(path, mode.value, encoding, bufferSize)
            }
        }
    }

    companion object {
        fun getSdcardPath(): String {
            return Files.getSdcardPath()
        }
    }

    enum class Mode(val value: String) {
        Read("r"),
        Write("w"),
        Append("a"),
        ReadWrite("rw"),
    }
}