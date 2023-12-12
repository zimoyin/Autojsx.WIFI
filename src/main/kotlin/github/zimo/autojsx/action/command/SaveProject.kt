package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.server.VertxServer
import github.zimo.autojsx.util.*
import io.vertx.core.json.JsonObject
import java.io.File

class SaveProject :
    AnAction("选择上传项目", "保存项目", github.zimo.autojsx.icons.ICONS.SAVE_16) {
    private val projectJSON = "project.json"
    override fun actionPerformed(e: AnActionEvent) {
        val dialog = FileChooserFactory.getInstance()
            .createFileChooser(FileChooserDescriptorFactory.createSingleFolderDescriptor(), e.project, null)

        val files = dialog.choose(e.project)

        if (files.isNotEmpty()) {
            val dir = files[0]
//            runProject(dir, e.project)
            zipProject(dir,e.project){
                VertxServer.Command.saveProject(it.zipPath)
                logI("项目正在上传: " + it.projectJsonPath)
                logI("正在上传 src: " + it.srcPath)
                logI("项目正在上传 resources: " + it.resourcesPath)
                logI("项目正在上传 lib: " + it.libPath+"\r\n")
            }
        }
    }

    @Deprecated("TODO")
    private fun runProject(file: VirtualFile, project: Project?) {
        val jsonFile = findFile(file, projectJSON)
        if (jsonFile != null) {
            val projectJson = File(jsonFile.path)
            val json = JsonObject(projectJson.readText())
            runServer(project)

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
//                zip.delete()
                logI("项目正在上传: " + projectJson.path)
                logI("正在上传 src: " + src.path)
                logI("项目正在上传 resources: " + resources.path)
                logI("项目正在上传 lib: " + lib.path)

            }
            return
        }
        logE("项目无法上传: 选择的文件夹没有包含项目描述文件 'project.json'")
    }
}