package github.zimo.autojsx.util

import NodeJsWithPool
import closeConsole
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import executeObject
import executeString
import getObjectConverter
import github.zimo.autojsx.server.ConsoleOutputV2
import github.zimo.autojsx.server.VertxCommandServer
import openConsole
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.SystemIndependent
import require
import setGlobalObject
import setNodeModuleRootDirectory
import java.awt.image.BufferedImage
import java.io.*
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.zip.ZipInputStream
import javax.imageio.ImageIO
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JOptionPane
import javax.swing.JPanel


/**
 * 在当前文件夹查询project.json文件
 *  存在项目文件： 执行特点操作
 *  不存在项目文件：上级查询文件/不存在则查找resources是否存在/存在则查询resources下的project.json/存在则执行/不存在则循环该操作一直到项目/模块根目录
 */
fun searchProjectJsonByEditor(project: Project?, executeSpecificOperation: (VirtualFile) -> Unit) {
    if (project != null) {
        val projectBasePath = project.basePath
        val fileEditorManager = FileEditorManager.getInstance(project)
        //保存正在修改的文件
        fileEditorManager.selectedFiles.apply {
            val documentManager = FileDocumentManager.getInstance()
            for (file in this) {
                val document: Document? = documentManager.getDocument(file!!)
                if (document != null) {
                    documentManager.saveDocument(document)
                }
            }
        }


        val selectedEditor = fileEditorManager.selectedEditor

        if (selectedEditor != null) {
            val selectedFile: VirtualFile = fileEditorManager.selectedFiles[0]
            val targetFileName = "project.json"
            var currentDir = selectedFile.parent

            while (currentDir != null && isProjectRoot(currentDir.path, projectBasePath)) {
                val targetFile = currentDir.findChild(targetFileName)
                if (targetFile != null) {
                    // 找到项目文件
                    ConsoleOutputV2.systemPrint("执行项目/I: $targetFile")
                    // 在这里执行特定操作
                    runCatching { executeSpecificOperation(targetFile) }.onFailure {
                        ConsoleOutputV2.systemPrint("执行项目文件失败 $targetFile  /E\r\n" + it.caseString())
                    }
                    return
                }

                // 如果当前目录中没有找到项目文件，则继续向上级目录查找
                val resourcesDir = currentDir.findChild("resources")
                if (resourcesDir != null) {
                    val resourcesFile = resourcesDir.findChild(targetFileName)
                    if (resourcesFile != null) {
                        // 在 resources 目录下找到项目文件
                        ConsoleOutputV2.systemPrint("执行项目/I: $resourcesFile")
                        // 在这里执行特定操作
                        runCatching { executeSpecificOperation(resourcesFile) }.onFailure {
                            ConsoleOutputV2.systemPrint("执行项目文件失败 $resourcesFile  /E\r\n" + it.caseString())
                        }
                        return
                    }
                }

                // 继续向上级目录查找
                currentDir = currentDir.parent
            }

            ConsoleOutputV2.systemPrint("$targetFileName not found in the parent paths.")
        } else {
            logW("未打开任何项目，请打开你的 main.js。插件需要再活动编辑器窗口看到他")
        }
    }
}

fun searchProjectJsonByFile(
    project: Project?,
    selectedFile: VirtualFile,
    executeSpecificOperation: (VirtualFile) -> Unit
) {
    if (project != null) {
        val projectBasePath = project.basePath
        val targetFileName = "project.json"
        var currentDir = selectedFile.parent

        while (currentDir != null && isProjectRoot(currentDir.path, projectBasePath)) {
            val targetFile = currentDir.findChild(targetFileName)
            if (targetFile != null) {
                // 找到项目文件
                ConsoleOutputV2.systemPrint("执行项目/I: $targetFile")
                // 在这里执行特定操作
                runCatching { executeSpecificOperation(targetFile) }.onFailure {
                    ConsoleOutputV2.systemPrint("执行项目文件失败 $targetFile  /E\r\n" + it.caseString())
                }
                return
            }

            // 如果当前目录中没有找到项目文件，则继续向上级目录查找
            val resourcesDir = currentDir.findChild("resources")
            if (resourcesDir != null) {
                val resourcesFile = resourcesDir.findChild(targetFileName)
                if (resourcesFile != null) {
                    // 在 resources 目录下找到项目文件
                    ConsoleOutputV2.systemPrint("执行项目/I: $resourcesFile")
                    // 在这里执行特定操作
                    runCatching { executeSpecificOperation(resourcesFile) }.onFailure {
                        ConsoleOutputV2.systemPrint("执行项目文件失败 $resourcesFile  /E\r\n" + it.caseString())
                    }
                    return
                }
            }

            // 继续向上级目录查找
            currentDir = currentDir.parent
        }

        ConsoleOutputV2.systemPrint("$targetFileName not found in the parent paths.")
    }
}

fun isProjectRoot(path: @NonNls String, projectBasePath: @SystemIndependent @NonNls String?): Boolean {
    return path != projectBasePath?.let { File(it).parent }
}

fun runServer(project: Project?) {
    if (!VertxCommandServer.isStart) {
        VertxCommandServer.start()
//        AutojsNotifier.info(
//            project,
//            "Autojsx 服务器在 ${VertxServer.getServerIpAddress()}:${VertxServer.port} 尝试启动"
//        )
        val toolWindowManager = ToolWindowManager.getInstance(project!!)
        toolWindowManager.getToolWindow("AutojsxConsole")?.apply {
            show()
        }
    }
}

fun stopServer(project: Project?) {
    if (VertxCommandServer.isStart) {
        VertxCommandServer.Command.stopAll()
        VertxCommandServer.stop()
//        AutojsNotifier.info(
//            project,
//            "Autojsx 服务器在 ${VertxServer.getServerIpAddress()}:${VertxServer.port} 尝试关闭"
//        )
    }
}

val executor: ExecutorService = Executors.newFixedThreadPool(3)

fun logE(msg: Any?, e: Throwable? = null) {
    ConsoleOutputV2.systemPrint("错误/E: $msg ${(if (e != null) "\r\n" else "") + e?.caseString()}")
}

fun logE(msg: Any?) {
    ConsoleOutputV2.systemPrint("错误/E: $msg")
}

fun logI(msg: Any?) {
    ConsoleOutputV2.systemPrint("信息/I: $msg")
}

fun logW(msg: Any?) {
    ConsoleOutputV2.systemPrint("警告/W: $msg")
}


fun selectDevice() {
    val map = HashMap<String, JCheckBox>()
    val panel = JPanel()
    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

    for (item in VertxCommandServer.devicesWs.keys) {
        val checkBox = JCheckBox(item)
        map[item] = checkBox
        panel.add(checkBox)
    }

    VertxCommandServer.selectDevicesWs.forEach {
        map[it.key]?.isSelected = true
    }


    val option = JOptionPane.showConfirmDialog(
        null, panel, "Selection Devices",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
    )

    if (option == JOptionPane.OK_OPTION) {
        val result = StringBuilder("Selection Devices:\n")
        for (component in panel.components) {
            if (component is JCheckBox) {
                val checkBox = component
                if (checkBox.isSelected) {
                    result.append("✔ ").append(checkBox.text).append("\n")
                } else {
                    result.append("□").append(checkBox.text).append("\n")
                }
            }
        }
        JOptionPane.showMessageDialog(null, result.toString(), "Selection Devices", JOptionPane.INFORMATION_MESSAGE)

        VertxCommandServer.selectDevicesWs.clear()
        map.forEach {
            if (it.value.isSelected) {
                VertxCommandServer.devicesWs[it.key]?.apply {
                    VertxCommandServer.selectDevicesWs[it.key] = this
                }
            }
        }
        logI("选中的设备为: ${VertxCommandServer.selectDevicesWs.keys}")
    }
}


fun runningScriptList(project: Project) {
    if (!VertxCommandServer.isStart || VertxCommandServer.selectDevicesWs.isEmpty()) {
        logW("服务器中未选中设备")
        return
    }
    var result = false
    executor.submit {
        ProgressManager.getInstance().runProcessWithProgressSynchronously<Any, RuntimeException>(
            {
                while (true) {
                    Thread.sleep(600)
                    if (result) break
                }
            },
            "正在等待网络结果",
            true,  // indeterminate
            project
        )
    }

    executor.submit {
        runServer(project)
        runningScriptCheckBox {
            result = true
        }
    }
}

fun runningScriptCheckBox(callback: () -> Unit = {}) {
    logI("正在查询待关闭的脚本")

    VertxCommandServer.Command.getRunningList({
        callback()
        Thread.sleep(600)
        val panel = JPanel()
        val list = ArrayList<JCheckBox>()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        if (it.isEmpty()) {
            val checkBox = JCheckBox("截止到当前没有任何脚本在运行")
            panel.add(checkBox)
        }
        for (item in it) {
            val checkBox = JCheckBox(item.sourceName)
            list.add(checkBox)
            panel.add(checkBox)
        }

        val option = JOptionPane.showConfirmDialog(
            null, panel, "Select Items",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        )

        if (option == JOptionPane.OK_OPTION) {
            val result = StringBuilder("Selected items:\n")
            for (component in panel.components) {
                if (component is JCheckBox) {
                    val checkBox = component
                    if (checkBox.isSelected) {
                        result.append("✔ ").append(checkBox.text).append("\n")
                    }
                }
            }
            JOptionPane.showMessageDialog(
                null,
                result.toString(),
                "Selection Result",
                JOptionPane.INFORMATION_MESSAGE
            )
            list.forEach {
                if (it.isSelected) {
                    VertxCommandServer.Command.stopScriptBySourceName(it.text)
                }
            }
        }
    })
    callback()
}


fun base64_image_toFile(
    base64: String,
    basePath: String,
    times: Long = System.currentTimeMillis(),
): File {
    val image = base64_image(base64)
    val file = File(basePath + File.separator + "/build-output/images/$times/${UUID.randomUUID()}.png")
    file.parentFile.mkdirs()
    ImageIO.write(image, "png", file)
    return file
}

fun base64_image(it: String): BufferedImage? {
    val bytes = Base64.getDecoder().decode(it)
    val image = ImageIO.read(ByteArrayInputStream(bytes))
    return image
}

/**
 * @param map 文件路径和内容的映射
 */
@Deprecated("")
fun obfuscates(map: HashMap<String, String>, outputPath: String): HashMap<String, String>? {
    var mapHix: HashMap<String, String>? = null
    NodeJsWithPool {
        val console = openConsole()
        setNodeModuleRootDirectory("./node_modules")
        getObjectConverter().setLargeScope()
        require("./obfuscator.js")
        setGlobalObject("map", map)
        mapHix = executeObject<HashMap<String, String>>(
            "obfuscator.obfuscateMultiple(map, obfuscator.obfuscator_options)"
        )
        console.closeConsole()
    }
    return mapHix
}


fun obfuscate(code: String, outputPath: String, obfuscatorFile0: File, modules0: File): String? {
    var hix: String? = null
    NodeJsWithPool {
        val console = openConsole()
//        setNodeModuleRootDirectory("./node_modules")
        setNodeModuleRootDirectory(modules0)
        getObjectConverter().setLargeScope()
//        require("./obfuscator.js")
        require(obfuscatorFile0)
//        MyModuleBuilder::class.java.classLoader.getResourceAsStream("SDK.zip")
        setGlobalObject("code", code)
        hix = executeString(
            "obfuscator.obfuscate(code, obfuscator.obfuscator_options)"
        )
        console.closeConsole()
    }
    return hix
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
