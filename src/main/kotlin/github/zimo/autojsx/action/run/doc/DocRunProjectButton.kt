package github.zimo.autojsx.action.run.doc

import com.intellij.codeInsight.codeVision.CodeVisionState.NotReady.result
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.progress.BackgroundTaskQueue
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.module.MODULE_TYPE_ID
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.util.*
import github.zimo.autojsx.window.AutojsxConsoleWindow
import io.vertx.core.json.JsonObject


/**
 * 运行当前脚本
 */
class DocRunProjectButton :
    AnAction(github.zimo.autojsx.icons.ICONS.START_16) {


    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        AutojsxConsoleWindow.show(project)
        runServer(project)
        if (project != null) {
            runCurrentProject(project)
        }
    }


    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible =
            (e.project?.modules?.count { ModuleType.get(it).id == MODULE_TYPE_ID } ?: 0) > 0
    }

    companion object {
        private var isShowCheckboxMessageDialog = true
        private var isRunGradleConsole = true
        private fun showCheckboxMessageDialog() {
            // 定义对话框的消息内容
            val message = "是否在Autox 窗口输出信息，而不是单独在构建窗口输出构建信息"
            // 定义对话框的标题
            val title = "是否输出构建信息"
            // 定义对话框的按钮选项
            val options = arrayOf("是", "否", "取消")
            // 定义复选框的文本内容
            val checkboxText = "记住我: 在此次IDEA运行中不在询问"
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
                    0 -> isRunGradleConsole = false
                    1 -> isRunGradleConsole = true
                }
                Messages.CANCEL
            }
        }

        fun runCurrentProject(
            project: Project,
        ) {
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
            val selectedFile: VirtualFile? = kotlin.runCatching { fileEditorManager.selectedFiles[0] }.getOrNull()
            var findFile: VirtualFile? = selectedFile
            // 向上查询 src 目录的父目录
            for (i in 0 until 6) {
                if (findFile == null) break
                if (findFile.parent == null || findFile.parent.canonicalPath == project.basePath) {
                    break
                }
                findFile = findFile.parent
                if (findFile.findDirectory("src") != null) {
                    break
                }
            }
            val isAutojsProject = if (findFile != null) {
                ZipProjectJsonInfo.findProjectJsonInfo(findFile, project)?.isAutoJsProject() ?: false
            } else {
                false
            }

            if (GradleUtils.isGradleProject(project) && !isAutojsProject) {
                showCheckboxMessageDialog()
                if (isRunGradleConsole) GradleUtils.runGradleCommandOnToolWindow(project, "compile") {
                    if (it) {
                        runProject(project)
                    } else {
                        logE("Gradle:compile 构建失败,请通过构建窗口查看错误信息")
                    }
                } else {
                    executor.execute {
                        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "正在构建项目", true) {
                            override fun run(indicator: ProgressIndicator) {
                                logI("开始构建 Gradle:compile")
                                GradleUtils.runGradleCommand(project, "compile") { result ->
                                    if (result.success) {
                                        logI("构建Gradle:compile 成功")
                                        runProject(project)
                                    } else {
                                        logE("Gradle:compile 构建失败\n${result.error}\n")
                                    }
                                }
                            }
                        })
                    }
                }
            } else {
                // 运行项目
                runCatching {
                    if (selectedFile == null) {
                        logE("无法运行项目，请选择一个文件")
                        return@runCatching
                    }
                    searchProjectJsonByFile(project, selectedFile) { file ->
                        zipProject(file, project, 6).apply {
                            logI("预运行项目: " + info.projectJson)
                            logI("├──> 项目 src: " + info.src?.canonicalPath)
                            logI("├──> 项目 resources: " + info.resources?.canonicalPath)
                            logI("└──> 项目 lib: " + info.lib?.canonicalPath + "\r\n")
                            VertxCommand.runProject(bytes, info.name)
                        }
                    }
                }.onFailure {
                    logE("js脚本网络引擎执行失败${selectedFile?.path} ", it)
                }
            }
        }

        private fun runProject(project: Project) {
            val outputMainJsPath = kotlin.runCatching {
                getGradleOutputMainJsPath(project, false)
            }.getOrNull()

            if (outputMainJsPath == null) {
                logW("Gradle 已经构建完成了，但是 IDEA 未能索引到构建后的文件。使用 File 进行加载")
                kotlin.runCatching {
                    val file = getGradleOutputMainJsPathAsFile(project, false)
                    VertxCommand.runProject(zipBytes(file.canonicalPath), file.path)
                }.onFailure {
                    logE("无法执行项目，无法正确加载编译后的文件")
                }
                return
            } else {
                logI(outputMainJsPath)
                val zip = zipProject(outputMainJsPath, project)
                VertxCommand.runProject(zip.bytes, zip.info.name)
            }
        }
    }
}