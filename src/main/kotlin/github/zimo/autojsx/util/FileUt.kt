package github.zimo.autojsx.util

import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.module.MyModuleBuilder
import github.zimo.autojsx.server.ConsoleOutputV2
import github.zimo.autojsx.server.MainVerticle
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

fun createSDK(file: VirtualFile) {
    val lib = file.createChildDirectory(file, "sdk")
    val buffer = ByteArray(1024)
    MyModuleBuilder::class.java.classLoader.getResourceAsStream("SDK.zip")?.apply {
        try {
            // 打开zip文件流
            val zipInputStream = ZipInputStream(this)

            // 逐个解压zip条目
            var zipEntry = zipInputStream.nextEntry
            while (zipEntry != null) {
                val newFile = if (!zipEntry.isDirectory) lib.createChildData(
                    this, zipEntry.name.substring(zipEntry.name.lastIndexOf("/") + 1)
                ) else lib

                // 如果条目是文件则创建文件
                if (!zipEntry.isDirectory) {
                    val fileOutputStream = FileOutputStream(newFile.path)
                    var len: Int
                    while (zipInputStream.read(buffer).also { len = it } > 0) {
                        fileOutputStream.write(buffer, 0, len)
                    }
                    fileOutputStream.close()
                }
                zipEntry = zipInputStream.nextEntry
            }
            zipInputStream.closeEntry()
            zipInputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun resource(path: String): URL? {
    return MainVerticle::class.java.classLoader.getResource(path)
}

fun resourceAsStream(path: String): InputStream? {
    return MainVerticle::class.java.classLoader.getResourceAsStream(path)
}


fun zip(filenames: ArrayList<String>, outputPath: String) {
    val list: HashSet<File> = HashSet()
    filenames.forEach {
        val file = File(it)
        if (file.exists() && file.isDirectory) file.listFiles()?.let { it1 -> list.addAll(it1) }
    }

    ZipOutputStream(FileOutputStream(File(outputPath))).use { zipOutputStream ->
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