package github.zimo.autojsx.action.run.doc

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import github.zimo.autojsx.module.MODULE_TYPE_ID
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.util.*
import github.zimo.autojsx.window.AutojsxConsoleWindow


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

    companion object{
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
            val selectedFile: VirtualFile = fileEditorManager.selectedFiles[0]
            var findFile: VirtualFile = selectedFile
            // 向上查询 src 目录的父目录
            for (i in 0 until 6) {
                if (findFile.parent == null || findFile.parent.canonicalPath == project.basePath) {
                    break
                }
                findFile = findFile.parent
                if (findFile.findDirectory("src") != null) {
                    break
                }
            }
            val isAutojsProject = ZipProjectJsonInfo.findProjectJsonInfo(findFile, project)?.isAutoJsProject() ?: false


            if (GradleUtils.isGradleProject(project) && !isAutojsProject) {
                GradleUtils.runGradleCommandOnToolWindow(project, "compile") {
                    if (it) {
                        logI(getGradleOutputMainJsPath(project))
                        val zip = zipProject(getGradleOutputMainJsPath(project), project)
                        VertxCommand.runProject(zip.bytes, zip.info.name)
                    } else {
                        logE("Gradle:buildMainJs 构建失败,请通过构建窗口查看错误信息")
                    }
                }
            } else {
                // 运行项目
                runCatching {
                    searchProjectJsonByFile(project, selectedFile) { file ->
                        zipProject(file, project).apply {
                            logI("预运行项目: " + info.projectJson)
                            logI("├──> 项目 src: " + info.src?.canonicalPath)
                            logI("├──> 项目 resources: " + info.resources?.canonicalPath)
                            logI("└──> 项目 lib: " + info.lib?.canonicalPath + "\r\n")
                            VertxCommand.runProject(bytes, info.name)
                        }
                    }
                }.onFailure {
                    logE("js脚本网络引擎执行失败${selectedFile.path} ", it)
                }
            }
        }
    }
}