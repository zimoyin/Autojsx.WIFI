package github.zimo.autojsx.util


import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager

import github.zimo.autojsx.server.ConsoleOutput
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.server.VertxServer
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.SystemIndependent
import java.awt.image.BufferedImage
import java.io.*
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.zip.ZipInputStream
import javax.imageio.ImageIO

fun Project.fileEditorManager(): FileEditorManager {
    return FileEditorManager.getInstance(this)
}

fun FileEditorManager.saveCurrentDocument() {
    selectedFiles.apply {
        val documentManager = FileDocumentManager.getInstance()
        ApplicationManager.getApplication().runReadAction {
            for (file in this) {
                file ?: continue
                kotlin.runCatching {
                    val document: Document? = documentManager.getDocument(file)
                    if (document != null) {
                        ApplicationManager.getApplication().invokeLater {
                            documentManager.saveDocument(document)
                        }
                    }
                }.onFailure {
                    logW("无法保存当前打开的文件",it)
                }
            }
        }
    }
}

/**
 * 在当前文件夹查询project.json文件
 *  存在项目文件： 执行特点操作
 *  不存在项目文件：上级查询文件/不存在则查找resources是否存在/存在则查询resources下的project.json/存在则执行/不存在则循环该操作一直到项目/模块根目录
 */
fun searchProjectJsonByEditor(project: Project?, executeSpecificOperation: (VirtualFile) -> Unit) {
    if (project != null) {
        val projectBasePath = project.basePath
        val fileEditorManager = project.fileEditorManager()
        //保存正在修改的文件
        fileEditorManager.saveCurrentDocument()


        val selectedEditor = fileEditorManager.selectedEditor

        if (selectedEditor != null) {
            val selectedFile: VirtualFile = fileEditorManager.selectedFiles[0]
            val targetFileName = "project.json"
            var currentDir = selectedFile.parent

            while (currentDir != null && isProjectRoot(currentDir.path, projectBasePath)) {
                val targetFile = currentDir.findChild(targetFileName)
                if (targetFile != null) {
                    // 找到项目文件
                    ConsoleOutput.systemPrint("执行项目/I: $targetFile")
                    // 在这里执行特定操作
                    runCatching { executeSpecificOperation(targetFile) }.onFailure {
                        ConsoleOutput.systemPrint("执行项目文件失败 $targetFile  /E\r\n" + it.caseString())
                    }
                    return
                }

                // 如果当前目录中没有找到项目文件，则继续向上级目录查找
                val resourcesDir = currentDir.findChild("resources")
                if (resourcesDir != null) {
                    val resourcesFile = resourcesDir.findChild(targetFileName)
                    if (resourcesFile != null) {
                        // 在 resources 目录下找到项目文件
                        ConsoleOutput.systemPrint("执行项目/I: $resourcesFile")
                        // 在这里执行特定操作
                        runCatching { executeSpecificOperation(resourcesFile) }.onFailure {
                            ConsoleOutput.systemPrint("执行项目文件失败 $resourcesFile  /E\r\n" + it.caseString())
                        }
                        return
                    }
                }

                // 继续向上级目录查找
                currentDir = currentDir.parent
            }

            ConsoleOutput.systemPrint("$targetFileName not found in the parent paths.")
        } else {
            logW("未打开任何项目，请打开你的 main.js。插件需要再活动编辑器窗口看到他")
        }
    }
}

/**
 * 根据当前文件，在父文件夹查找 resources/project.json 循环操作知道找到
 * 注意: 该方法只适用于 Autojs 原生项目查找文件
 */
fun searchProjectJsonByFile(
    project: Project?,
    selectedFile: VirtualFile,
    executeSpecificOperation: (VirtualFile) -> Unit
) {
    if (project != null) {
        val projectBasePath = project.basePath
        val targetFileName = "project.json"
        var currentDir = if (selectedFile.isFile) selectedFile.parent else selectedFile

        while (currentDir != null && isProjectRoot(currentDir.path, projectBasePath)) {
            val targetFile = currentDir.findChild(targetFileName)
            if (targetFile != null) {
                // 在这里执行特定操作
                runCatching { executeSpecificOperation(targetFile) }.onFailure {
                    ConsoleOutput.systemPrint("执行项目文件失败 $targetFile  /E\r\n" + it.caseString())
                }
                return
            }

            // 如果当前目录中没有找到项目文件，则继续向上级目录查找
            val resourcesDir = currentDir.findChild("resources")
            if (resourcesDir != null) {
                val resourcesFile = resourcesDir.findChild(targetFileName)
                if (resourcesFile != null) {
                    // 在这里执行特定操作
                    runCatching { executeSpecificOperation(resourcesFile) }.onFailure {
                        ConsoleOutput.systemPrint("执行项目文件失败 $resourcesFile  /E\r\n" + it.caseString())
                    }
                    return
                }
            }

            // 继续向上级目录查找
            currentDir = currentDir.parent
        }

        ConsoleOutput.systemPrint("$targetFileName not found in the parent paths.")
    }
}

fun isProjectRoot(path: @NonNls String, projectBasePath: @SystemIndependent @NonNls String?): Boolean {
    return path != projectBasePath?.let { File(it).parent }
}

fun runServer(project: Project?) {
    if (!VertxServer.isStart) {
        VertxServer.start()
        val toolWindowManager = ToolWindowManager.getInstance(project!!)
        toolWindowManager.getToolWindow("AutojsxConsole")?.apply {
            show()
        }
    }
}

fun stopServer(project: Project?) {
    if (VertxServer.isStart) {
        VertxCommand.stopAll()
    }
    VertxServer.stop()
}

val executor: ExecutorService = Executors.newFixedThreadPool(8)


fun base64_image_toFile(
    base64: String,
    basePath: String,
    times: Long = System.currentTimeMillis(),
): File {
    val image = base64_image(base64)
    val file = File(basePath + File.separator + "/build/autojs/images/$times.png")
    file.parentFile.mkdirs()
    ImageIO.write(image, "png", file)
    return file
}

fun base64_image(it: String): BufferedImage? {
    val bytes = Base64.getDecoder().decode(it)
    val image = ImageIO.read(ByteArrayInputStream(bytes))
    return image
}

fun File.unzip(destinationDir: File) {
    val zipFile = this
    FileInputStream(zipFile).use { inputStream ->
        ZipInputStream(inputStream).use { zipInputStream ->
            var entry = zipInputStream.getNextEntry()
            while (entry != null) {
                val fileName = entry.name
                val file = File(destinationDir, fileName)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    FileOutputStream(file).use { outputStream ->
                        val buffer = ByteArray(4096)
                        var readBytes: Int
                        while (zipInputStream.read(buffer).also { readBytes = it } > 0) {
                            outputStream.write(buffer, 0, readBytes)
                        }
                    }
                }
                entry = zipInputStream.getNextEntry()
            }
        }
    }
}


fun InputStream.unzip(destinationDir: File) {
    val inputStream = this
    ZipInputStream(inputStream).use { zipInputStream ->
        var entry = zipInputStream.getNextEntry()
        while (entry != null) {
            val fileName = entry.name
            val file = File(destinationDir, fileName)
            if (entry.isDirectory) {
                file.mkdirs()
            } else {
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var readBytes: Int
                    while (zipInputStream.read(buffer).also { readBytes = it } > 0) {
                        outputStream.write(buffer, 0, readBytes)
                    }
                }
            }
            entry = zipInputStream.getNextEntry()
        }
    }
}
