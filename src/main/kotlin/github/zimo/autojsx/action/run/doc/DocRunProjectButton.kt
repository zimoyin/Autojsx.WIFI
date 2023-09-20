package github.zimo.autojsx.action.run.doc

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.server.VertxServer
import github.zimo.autojsx.util.*
import io.vertx.core.json.JsonObject
import java.io.File

/**
 * 运行当前脚本
 */
class DocRunProjectButton :
    AnAction(github.zimo.autojsx.icons.ICONS.START_16) {
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
                    searchProjectJsonByFile(project,selectedFile) { file ->
                        val projectJson = File(file.path)
                        val json = JsonObject(projectJson.readText())

                        val name = json.getString("name")
                        val src = projectJson.resolve(json.getString("srcPath")).canonicalFile
                        val resources = projectJson.resolve(json.getString("resources")).canonicalFile
                        val lib = projectJson.resolve(json.getString("lib")).canonicalFile

                        val zip = File(project?.basePath + "/build-output" + "/${name}.zip")
                        zip.parentFile.mkdirs()
                        if(zip.exists()) zip.delete()

                        executor.submit {
                            zip(
                                arrayListOf(src.path, resources.path, lib.path),
                                project?.basePath + File.separator + "build-output" + File.separator + "${name}.zip"
                            )

                            VertxServer.Command.runProject(zip.canonicalPath)
                            logI("项目正在上传: " + projectJson.path)
                            logI("正在上传 src: " + src.path)
                            logI("项目正在上传 resources: " + resources.path)
                            logI("项目正在上传 lib: " + lib.path)
                            logI("项目上传完成" + "\r\n")
//                            if(zip.exists()) zip.delete()
                        }
                    }
                }.onFailure {
                    logE("js脚本网络引擎执行失败${selectedFile.path} ", it)
                }
            }
        }
    }
}