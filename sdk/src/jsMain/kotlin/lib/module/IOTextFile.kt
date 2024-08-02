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
external interface ReadableTextFile {
    fun read(): String
    fun read(maxCount: Int): String
    fun readline(): String
    fun readlines(): Array<String>
    fun close()
}

@Deprecated("Use PReadableTextFile instead")
external interface WritableTextFile {
    fun write(text: String)
    fun writeline(line: String)
    fun writelines(lines: Array<String>)
    fun flush()
    fun close()
}
