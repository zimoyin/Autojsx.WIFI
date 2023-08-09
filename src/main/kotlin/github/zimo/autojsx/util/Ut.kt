package github.zimo.autojsx.util

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.server.AutojsNotifier
import github.zimo.autojsx.server.ConsoleOutputV2
import github.zimo.autojsx.server.VertxServer
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.SystemIndependent
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JOptionPane
import javax.swing.JPanel


/**
 * 在当前文件夹查询project.json文件
 *  存在项目文件： 执行特点操作
 *  不存在项目文件：上级查询文件/不存在则查找resources是否存在/存在则查询resources下的project.json/存在则执行/不存在则循环该操作一直到项目/模块根目录
 */
fun searchProjectJSON(project: Project?, executeSpecificOperation: (VirtualFile) -> Unit) {
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
            ConsoleOutputV2.systemPrint("为打开任何项目")
        }
    }
}

fun isProjectRoot(path: @NonNls String, projectBasePath: @SystemIndependent @NonNls String?): Boolean {
    return path != projectBasePath?.let { File(it).parent }
}

fun runServer(project: Project?) {
    if (!VertxServer.isStart) {
        VertxServer.start()
        AutojsNotifier.info(
            project,
            "Autojsx 服务器在 ${VertxServer.getServerIpAddress()}:${VertxServer.port} 尝试启动"
        )
    }
}
fun stopServer(project: Project?) {
    if (VertxServer.isStart) {
        VertxServer.stop()
        AutojsNotifier.info(
            project,
            "Autojsx 服务器在 ${VertxServer.getServerIpAddress()}:${VertxServer.port} 尝试关闭"
        )
    }
}

val executor: ExecutorService = Executors.newFixedThreadPool(3)

fun logE(msg:Any,e:Throwable?=null){
    ConsoleOutputV2.systemPrint("错误/E: $msg ${"\r\n"+e?.caseString()}")
}
fun logI(msg:Any){
    ConsoleOutputV2.systemPrint("信息/I: $msg")
}fun logW(msg:Any){
    ConsoleOutputV2.systemPrint("警告/W: $msg")
}






fun selectDevice(){
    val map = HashMap<String, JCheckBox>()
    val panel = JPanel()
    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

    for (item in VertxServer.devicesWs.keys) {
        val checkBox = JCheckBox(item)
        map[item] = checkBox
        panel.add(checkBox)
    }

    VertxServer.selectDevicesWs.forEach {
        map[it.key]?.isSelected = true
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
                } else {
                    result.append("□").append(checkBox.text).append("\n")
                }
            }
        }
        JOptionPane.showMessageDialog(null, result.toString(), "Selection Result", JOptionPane.INFORMATION_MESSAGE)

        VertxServer.selectDevicesWs.clear()
        map.forEach {
            if (it.value.isSelected) {
                VertxServer.devicesWs[it.key]?.apply {
                    VertxServer.selectDevicesWs[it.key] = this
                }
            }
        }
        logI("选中的设备为: ${VertxServer.selectDevicesWs.keys}")
    }
}