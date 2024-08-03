package lib.module


/**
 *
 * @author : zimo
 * @date : 2024/07/30
 */

external interface PReadableTextFile {
    fun read(): String
    fun read(maxCount: Int): String
    fun readline(): String
    fun readlines(): Array<String>
    fun write(text: String)
    fun writeline(line: String)
    fun writelines(lines: Array<String>)
    fun flush()
    fun close()
}

@Deprecated("Use PReadableTextFile instead")
external interface ReadableTextFile : PReadableTextFile {
    override fun read(): String
    override fun read(maxCount: Int): String
    override fun readline(): String
    override fun readlines(): Array<String>
    override fun close()
}

fun ReadableTextFile.write() {
    throw RuntimeException("读对象禁止写操作")
}
fun ReadableTextFile.writeline() {
    throw RuntimeException("读对象禁止写操作")
}
fun ReadableTextFile.writelines() {
    throw RuntimeException("读对象禁止写操作")
}
fun ReadableTextFile.flush() {
    throw RuntimeException("读对象禁止写操作")
}

@Deprecated("Use PReadableTextFile instead")
external interface WritableTextFile : PReadableTextFile {
    override fun write(text: String)
    override fun writeline(line: String)
    override fun writelines(lines: Array<String>)
    override fun flush()
    override fun close()
}

fun WritableTextFile.read() {
    throw RuntimeException("写对象禁止读操作")
}
fun WritableTextFile.read(maxCount: Int) {
    throw RuntimeException("写对象禁止读操作")
}
fun WritableTextFile.readline() {
    throw RuntimeException("写对象禁止读操作")
}
fun WritableTextFile.readlines() {
    throw RuntimeException("写对象禁止读操作")
}
