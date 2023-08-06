package github.zimo.autojsx.action.news

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.Messages.showCheckboxMessageDialog
import com.intellij.openapi.vfs.VirtualFileManager
import github.zimo.autojsx.module.MyModuleType
import github.zimo.autojsx.util.createSDK
import javax.swing.ImageIcon

/**
 * 新建项目的项目描述文件 json
 */
class NewAutoJSON :
    AnAction(ImageIcon(MyModuleType::class.java.classLoader.getResource("icons/pluginMainDeclaration.png"))) {
    override fun actionPerformed(e: AnActionEvent) {
        val action = this
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file == null || !file.isDirectory) return
        //构建结构
        WriteCommandAction.runWriteCommandAction(e.project) {
            file.createChildData(this, "project.json").getOutputStream(this).use { outputStream ->
                outputStream.write(
                    """
                            |{
                            |    "name": "${file.name}",
                            |    "main": "main.js",
                            |    "ignore": [
                            |        "build"
                            |    ],
                            |    "launchConfig": {
                            |        "hideLogs": true
                            |    },
                            |    "packageName": "github.autojsx.${file.name}",
                            |    "versionName": "1.0.0",
                            |    "versionCode": 1
                            |}
                        """.trimMargin().toByteArray()
                )
            }
            // 刷新虚拟文件系统，以确保新创建的文件可见
            VirtualFileManager.getInstance().refreshWithoutFileWatcher(false)
        }
    }
}