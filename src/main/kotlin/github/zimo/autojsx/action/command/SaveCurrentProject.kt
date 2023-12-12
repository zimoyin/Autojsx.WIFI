package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.server.VertxServer
import github.zimo.autojsx.util.*
import io.vertx.core.json.JsonObject
import java.io.File

class SaveCurrentProject :
    AnAction("保存当前项目", "保存当前项目", github.zimo.autojsx.icons.ICONS.SAVE_RUN_16) {
    private val projectJSON = "project.json"
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        searchProjectJsonByEditor(project) { file ->
//            saveProject(file, project)
            zipProject(file,e.project){
                VertxServer.Command.saveProject(it.zipPath)
                VertxServer.Command.runProject(it.zipPath)
                logI("项目正在上传: " + it.projectJsonPath)
                logI("正在上传 src: " + it.srcPath)
                logI("项目正在上传 resources: " + it.resourcesPath)
                logI("项目正在上传 lib: " + it.libPath+"\r\n")
            }
        }
    }

    @Deprecated("TODO")
    private fun saveProject(file: VirtualFile, project: Project?) {
        val projectJson = File(file.path)
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
            VertxServer.Command.runProject(zip.canonicalPath)
            logI("项目正在上传: " + projectJson.path)
            logI("正在上传 src: " + src.path)
            logI("项目正在上传 resources: " + resources.path)
            logI("项目正在上传 lib: " + lib.path + "\r\n")
    //                if (zip.exists()) zip.delete()
        }
    }
}