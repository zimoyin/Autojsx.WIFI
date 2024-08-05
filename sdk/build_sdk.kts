import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun zipFiles(sourceFiles: List<File>, outputFile: File) {
    ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
        sourceFiles.forEach { file ->
            zipFile(file, file.name, zipOut)
        }
    }
}

fun zipFile(fileToZip: File, fileName: String, zipOut: ZipOutputStream) {
    if (fileToZip.isHidden) {
        return
    }
    if (fileToZip.isDirectory) {
        val children = fileToZip.listFiles()
        children?.forEach { childFile ->
            zipFile(childFile, "$fileName/${childFile.name}", zipOut)
        }
        return
    }
    FileInputStream(fileToZip).use { fis ->
        val zipEntry = ZipEntry(fileName)
        zipOut.putNextEntry(zipEntry)
        fis.copyTo(zipOut)
    }
}

val sourceFiles = listOf(
    File(".idea"),
    File("config"),
    File("src"),

    File("build.gradle.kts"),
    File("gradle.properties"),
    File("gradlew"),
    File("gradlew.bat"),
    File("settings.gradle.kts"),

    File("README.md")
//    File("LICENSE"),
)

val outputZipFile = File("../src/main/resources/KotlinAndJs.zip")

outputZipFile.parentFile.mkdirs() // 创建父目录（如果不存在）

zipFiles(sourceFiles, outputZipFile)

println("ZIP file created at: ${outputZipFile.absolutePath}")
