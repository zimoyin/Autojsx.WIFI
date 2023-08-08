package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.server.ConsoleOutputV2
import github.zimo.autojsx.server.VertxServer
import github.zimo.autojsx.util.caseString
import github.zimo.autojsx.util.runServer

/**
 * 保存当前文件到app 的根路径，注意保存的也许是二进制文件
 */
class SaveScript :
    AnAction("保存当前文件", "保存文件", github.zimo.autojsx.icons.ICONS.SAVE_FILE_16) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        runServer(project)
        if (project != null) {
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
            //获取正在编辑的文件
            val selectedEditor = fileEditorManager.selectedEditor

            if (selectedEditor != null) {
                val selectedFile: VirtualFile = fileEditorManager.selectedFiles[0]
                runCatching {
                    VertxServer.Command.saveJS(selectedFile.path)
                }.onFailure {
                    ConsoleOutputV2.systemPrint("js脚本网络引擎执行失败${selectedFile.path} /E \r\n" + it.caseString())
                }
            }
        }
    }
}