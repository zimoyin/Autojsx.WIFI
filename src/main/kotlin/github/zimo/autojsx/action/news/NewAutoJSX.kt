package github.zimo.autojsx.action.news

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.modules
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import com.jetbrains.rd.util.getLogger
import com.jetbrains.rd.util.warn
import github.zimo.autojsx.icons.ICONS
import github.zimo.autojsx.module.MODULE_TYPE_ID
import github.zimo.autojsx.util.createSDK
import github.zimo.autojsx.util.logI


class NewAutoJSX :
    AnAction(github.zimo.autojsx.icons.ICONS.LOGO_16) {

    private var isShowCheckboxMessageDialog = true
    private var isCreateSDK = true
    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file == null || !file.isDirectory) return
        val userInputFileName: String? =
            Messages.showInputDialog(e.project, "请输入文件名:", "新建文件", Messages.getQuestionIcon())?.trim()
        if (userInputFileName.isNullOrEmpty()) return
        showCheckboxMessageDialog()

        //构建结构
        WriteCommandAction.runWriteCommandAction(e.project) {
            file.createChildDirectory(this, userInputFileName).apply {
                createChildDirectory(this, "lib").apply {
                    if (isCreateSDK) createSDK(this)
                }

                createChildDirectory(this, "src").apply {
                    createChildData(this, "main.js").getOutputStream(this).use { outputStream ->
                        outputStream.write(
                            ("(function main() {\n" + "    let path = files.path(\"./test.txt\")\n" + "    log(files.read(path))\n" + "})()").toByteArray()
                        )
                    }
                }

                createChildDirectory(this, "resources").apply {
                    createChildData(this, "test.txt").getOutputStream(this).use { outputStream ->
                        outputStream.write("Hello Autojsx!".toByteArray())
                    }
                    createChildData(this, "project.json").getOutputStream(this).use { outputStream ->
                        outputStream.write(
                            """
                            |{
                            |    "name": "$userInputFileName",
                            |    "main": "main.js",
                            |    "ignore": [
                            |        "build"
                            |    ],
                            |    "launchConfig": {
                            |        "hideLogs": true
                            |    },
                            |    "packageName": "github.autojsx.$userInputFileName",
                            |    "versionName": "1.0.0",
                            |    "srcPath":"./../../src/",
                            |    "resources":"./../",
                            |    "lib":"./../../lib/",
                            |    "versionCode": 1,
                            |    "obfuscator": false,
                            |    "obfuscatorPath": "./obfuscator.js"
                            |}
                        """.trimMargin().toByteArray()
                        )
                    }
                    createChildData(this, "obfuscator.js").getOutputStream(this).use { outputStream ->
                        ICONS::class.java.getResourceAsStream("/obfuscator.js")?.readAllBytes()?.let {
                            outputStream.write(it)
                        }
                    }
                }
            }
            // 刷新虚拟文件系统，以确保新创建的文件可见
            VirtualFileManager.getInstance().refreshWithoutFileWatcher(false)
        }
    }


    private fun showCheckboxMessageDialog() {
        // 定义对话框的消息内容
        val message = "是否创建SDK"
        // 定义对话框的标题
        val title = "SDK引导"
        // 定义对话框的按钮选项
        val options = arrayOf("是", "否", "取消")
        // 定义复选框的文本内容
        val checkboxText = "记住我: 在此次运行中不在询问"
        // 定义复选框的初始选中状态
        val isChecked = false
        // 定义默认选中的按钮选项的索引
        val defaultOptionIndex = 0
        // 定义对话框打开时，焦点应该聚焦的按钮选项的索引
        val focusedOptionIndex = 0
        // 定义对话框中显示的图标
        val icon = Messages.getQuestionIcon()
        // 调用 showCheckboxMessageDialog 方法，并获取用户点击的按钮选项的索引
        if (isShowCheckboxMessageDialog) Messages.showCheckboxMessageDialog(
            message, title, options, checkboxText, isChecked, defaultOptionIndex, focusedOptionIndex, icon
        ) { buttonIndex, checkBox ->
            // 判断复选框是否被选中
            isShowCheckboxMessageDialog = !checkBox.isSelected
            //单击的按钮的按钮
            when (buttonIndex) {
                0 -> isCreateSDK = true
                1 -> isCreateSDK = false
            }
            Messages.CANCEL
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = (e.project?.modules?.count { ModuleType.get(it).id == MODULE_TYPE_ID } ?: 0) > 0
    }
}