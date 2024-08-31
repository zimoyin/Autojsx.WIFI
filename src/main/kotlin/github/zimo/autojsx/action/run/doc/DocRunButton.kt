package github.zimo.autojsx.action.run.doc

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.modules
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.module.MODULE_TYPE_ID
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.util.fileEditorManager
import github.zimo.autojsx.util.logE
import github.zimo.autojsx.util.runServer
import github.zimo.autojsx.util.saveCurrentDocument
import github.zimo.autojsx.window.AutojsxConsoleWindow


/**
 * 运行当前脚本
 */
class DocRunButton :
    AnAction(github.zimo.autojsx.icons.ICONS.START_16) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        AutojsxConsoleWindow.show(project)
        runServer(project)
        if (project != null) {
            val fileEditorManager = project.fileEditorManager()
            //保存正在修改的文件
            fileEditorManager.saveCurrentDocument()

            //获取正在编辑的文件
            val selectedEditor = fileEditorManager.selectedEditor

            if (selectedEditor != null) {
                val selectedFile: VirtualFile = fileEditorManager.selectedFiles[0]
                runCatching {
                    VertxCommand.rerunJS(selectedFile.path)
                    AutojsxConsoleWindow.show(project)
                }.onFailure {
                    logE("js脚本网络引擎执行失败${selectedFile.path} ", it)
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val isJsFile = file != null && "js" == file.extension
        e.presentation.isEnabledAndVisible = isJsFile && (e.project?.modules?.count { ModuleType.get(it).id == MODULE_TYPE_ID } ?: 0) > 0
    }
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}