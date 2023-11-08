package github.zimo.autojsx.action.save

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.server.ConsoleOutputV2
import github.zimo.autojsx.server.VertxServer
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
        } else {//保存文件
            runCatching {
                if (file.name == ProjectJSON) {
                    showCheckboxMessageDialog()
                    if (isSaveProject) {
                        saveDir(file.parent, e.project, false)
                        return
                    }
                }
                VertxServer.Command.saveJS(file.path)
            }.onFailure {
                ConsoleOutputV2.systemPrint("js脚本网络引擎执行失败${file.path} /E \r\n" + it.caseString())
            }
        }
    }

    fun saveDir(file: VirtualFile, project: Project?, showDialog: Boolean = true) {
        val jsonFile = findFile(file, ProjectJSON)
        if (jsonFile != null) {
            if (showDialog) showCheckboxMessageDialog()
            if (isSaveProject) {
                val projectJson = File(jsonFile.path)
                val json = JsonObject(projectJson.readText())

                val name = json.getString("name")
                val src = projectJson.resolve(json.getString("srcPath")).canonicalFile
                //TODO 创建临时混淆目录，并混淆，如果开启了混淆
                val resources = projectJson.resolve(json.getString("resources")).canonicalFile
                val lib = projectJson.resolve(json.getString("lib")).canonicalFile

                val zip = File(project?.basePath + "/build-output" + "/${name}.zip")
                zip.parentFile.mkdirs()
                if (zip.exists()) zip.delete()

                executor.submit {
                    zip(
                        arrayListOf(src.path, resources.path, lib.path),
                        project?.basePath + File.separator + "build-output" + File.separator + "${name}.zip"
                    )
                    VertxServer.Command.saveProject(zip.canonicalPath)
                    ConsoleOutputV2.systemPrint("项目正在上传/I: " + projectJson.path)
                    logI("正在上传 src: " + src.path)
                    logI("项目正在上传 resources: " + resources.path)
                    logI("项目正在上传 lib: " + lib.path+"\r\n")
//                    if (zip.exists()) zip.delete()
                }
                return
            }
        }

        executor.submit {
            val zip = File(project?.basePath + File.separator + "build-output" + File.separator + "${file.name}.zip")
            zip(
                arrayListOf(file.path),
                project?.basePath + File.separator + "build-output" + File.separator + "${file.name}.zip"
            )
            VertxServer.Command.saveProject(zip.canonicalPath)
            zip.delete()
            ConsoleOutputV2.systemPrint("项目正在上传/I: " + file.path)
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
}