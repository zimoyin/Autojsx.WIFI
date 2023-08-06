package github.zimo.autojsx.util

import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.module.MyModuleBuilder
import github.zimo.autojsx.module.MyModuleType
import github.zimo.autojsx.server.MainVerticle
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.util.zip.ZipInputStream

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