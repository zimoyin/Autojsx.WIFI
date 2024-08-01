package github.zimo.autojsx.util

import com.intellij.internal.statistic.uploader.ExternalDataCollectorLogger.findDirectory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import github.zimo.autojsx.server.MainVerticle
import java.io.*
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


fun resource(path: String): URL? {
    return MainVerticle::class.java.classLoader.getResource(path)
}

fun resourceAsStream(path: String): InputStream? {
    return MainVerticle::class.java.classLoader.getResourceAsStream(path)
}

/**
 * 压缩文件
 * @param path 需要压缩的文件路径
 */
fun zipBytes(path: String): ByteArray {
    val stream = ByteArrayOutputStream()
    zip(arrayListOf(path), null, stream)
    return stream.toByteArray()
}

/**
 * 压缩文件
 * @param path 需要压缩的文件路径
 * @param outputPath 压缩后的文件路径，outputPath 和 byteArrayOutputStream 二选一
 * @param byteArrayOutputStream 压缩后的文件字节流
 */
fun zip(path: String, outputPath: String? = null, byteArrayOutputStream: ByteArrayOutputStream? = null) {
    zip(arrayListOf(path), outputPath, byteArrayOutputStream)
}

/**
 * 压缩文件
 * @param filenames 需要压缩的文件列表
 * @param outputPath 压缩后的文件路径，outputPath 和 byteArrayOutputStream 二选一
 * @param byteArrayOutputStream 压缩后的文件字节流
 */
fun zip(
    filenames: ArrayList<String>,
    outputPath: String? = null,
    byteArrayOutputStream: ByteArrayOutputStream? = null
) {
    val list: HashSet<File> = HashSet()
    filenames.forEach {
        val file = File(it)
        if (file.exists() && file.isDirectory) file.listFiles()?.let { it1 -> list.addAll(it1) }
    }
    val outputStream = byteArrayOutputStream
        ?: (outputPath?.let { FileOutputStream(File(it)) }
            ?: throw IllegalArgumentException("outputPath or byteArrayOutputStream must not be null"))

    ZipOutputStream(outputStream).use { zipOutputStream ->
        for (file in list) {
            if (file.name != "sdk") addFileToZip(zipOutputStream, file, "")
        }
    }
}

fun addFileToZip(zipOutputStream: ZipOutputStream, file: File, parentDir: String) {
    val entryName = if (parentDir.isEmpty()) file.name else "$parentDir/${file.name}"
    if (file.isDirectory) {
        val files = file.listFiles()
        if (files != null) {
            for (childFile in files) {
                addFileToZip(zipOutputStream, childFile, entryName)
            }
        }
    } else {
        val buffer = ByteArray(1024)
        FileInputStream(file).use { inputStream ->
            zipOutputStream.putNextEntry(ZipEntry(entryName))
            var length = inputStream.read(buffer)
            while (length > 0) {
                zipOutputStream.write(buffer, 0, length)
                length = inputStream.read(buffer)
            }
            zipOutputStream.closeEntry()
        }
    }
}


fun findFile(rootDir: VirtualFile, fileName: String): VirtualFile? {
    return findInDirRecursive(rootDir, fileName)
}

private fun findInDirRecursive(dir: VirtualFile, filename: String?): VirtualFile? {

    // 1. 在当前目录查找指定文件
    var file = dir.findChild(filename!!)
    if (file != null) {
        return file
    }

    // 2. 获取当前目录的子目录
    val children = VfsUtil.getChildren(dir)

    // 3. 优先查找resources子目录
    for (child in children) {
        if (child.name == "resources") {
            file = findInDirRecursive(child, filename)
            if (file != null) {
                return file
            }
        }
    }

    // 4. 递归搜索每个子目录
    for (child in children) {
        if (child.isDirectory) {
            file = findInDirRecursive(child, filename)
            if (file != null) {
                return file
            }
        }
    }
    return null
}

fun getGradleOutputMainJsPath(project: Project): VirtualFile {
    return (project.projectFile ?: project.workspaceFile)?.parent?.parent
        ?.let { project.basePath?.let { it1 -> VfsUtil.createDirectoryIfMissing(it1) } }
        ?.findDirectory("build")
        ?.findDirectory("autojs")
        ?.findDirectory("compilation") ?: throw IllegalArgumentException("build/autojs/compilation directory is null")
}