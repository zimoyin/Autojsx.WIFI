package github.zimo.autojsx.action.save

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.module.MODULE_TYPE_ID
import github.zimo.autojsx.server.ConsoleOutput
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.util.*
import io.vertx.core.json.JsonObject
import java.io.File


class Save :
    AnAction(github.zimo.autojsx.icons.ICONS.SAVE_16) {
    private val ProjectJSON = "project.json"

    var isShowCheckboxMessageDialog = true
    var isSaveProject = true

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        runServer(e.project)
        //保存文件夹
        if (file.isDirectory) {
            saveDir(file, e.project, true)
            logI("js 文件保存成功: ${file.path}")
        } else {//保存文件
            runCatching {
                if (file.name == ProjectJSON) {
                    showCheckboxMessageDialog()
                    if (isSaveProject) {
                        saveDir(file.parent, e.project, false)
                        return
                    }
                }
                VertxCommand.saveJS(file.path)
            }.onFailure {
                ConsoleOutput.systemPrint("js脚本网络引擎执行失败${file.path} /E \r\n" + it.caseString())
            }
        }
    }

    fun saveDir(file: VirtualFile, project: Project?, showDialog: Boolean = true) {
        val jsonFile = findFile(file, ProjectJSON)
        if (jsonFile != null) {
            if (showDialog) showCheckboxMessageDialog()
            if (isSaveProject) {
                zipProject(jsonFile,project).apply{
                    logI("预运行项目: " + info.projectJson)
                    logI("├──> 项目 src: " + info.src?.canonicalPath)
                    logI("├──> 项目 resources: " + info.resources?.canonicalPath)
                    logI("└──> 项目 lib: " + info.lib?.canonicalPath + "\r\n")
                    VertxCommand.runProject(bytes, info.name)
                }
                // 保存项目压缩包完成后就不执行下面的上传文件夹逻辑
                return
            }
        }

        // 如果不是项目文件，就上次文件夹压缩包
        executor.submit {
            val zip = File(project?.basePath + File.separator + "build-output" + File.separator + "${file.name}.zip")
            zip(
                arrayListOf(file.path),
                project?.basePath + File.separator + "build-output" + File.separator + "${file.name}.zip"
            )
            VertxCommand.saveProject(zip.canonicalPath)
            ConsoleOutput.systemPrint("项目正在上传/I: " + file.path)
        }
    }

    private fun showCheckboxMessageDialog() {
        // 定义对话框的消息内容
        val message = "是否保存检测到的项目"
        // 定义对话框的标题
        val title = "保存引导"
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
                0 -> isSaveProject = true
                1 -> isSaveProject = false
            }
            Messages.CANCEL
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = (e.project?.modules?.count { ModuleType.get(it).id == MODULE_TYPE_ID } ?: 0) > 0
    }
}