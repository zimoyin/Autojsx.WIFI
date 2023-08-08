import github.zimo.autojsx.util.zip
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun main() {
    val filenames = arrayListOf<String>("build/libs", "build/kotlin", "build/snapshot")
    val zipFile = File("zipfile.zip")

    zip(filenames, "zipfile.zip")

    println("文件夹压缩完成：${zipFile.absolutePath}")

}




