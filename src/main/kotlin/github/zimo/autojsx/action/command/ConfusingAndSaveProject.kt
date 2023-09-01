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

//TODO
@Deprecated("Please use")
class ConfusingAndSaveProject :
    AnAction("混淆并保存项目", "混淆并保存项目", github.zimo.autojsx.icons.ICONS.SAVE_16) {
    private val projectJSON = "project.json"
    override fun actionPerformed(e: AnActionEvent) {
        val dialog = FileChooserFactory.getInstance()
            .createFileChooser(FileChooserDescriptorFactory.createSingleFolderDescriptor(), e.project, null)

        val files = dialog.choose(e.project)

        if (files.isNotEmpty()) {
            val dir = files[0]
            runProject(dir, e.project)
        }
    }

    private fun runProject(file: VirtualFile, project: Project?) {
        val jsonFile = findFile(file, projectJSON)
        if (jsonFile != null) {
            val projectJson = File(jsonFile.path)
            val json = JsonObject(projectJson.readText())
            runServer(project)
            val name = json.getString("name")
            val src = projectJson.resolve(json.getString("srcPath")).canonicalFile
            val resources = projectJson.resolve(json.getString("resources")).canonicalFile
            val lib = projectJson.resolve(json.getString("lib")).canonicalFile

            val zip = File(project?.basePath + "/build-output" + "/${name}.zip")
            val confusingSrc = File(zip.parentFile.canonicalPath + File.separator + "/confusing-$name")


            zip.parentFile.mkdirs()
            zip.delete()

            executor.submit {
                //阻塞等待
                if (!VertxServer.Command.confusion(src, confusingSrc)) {
                    return@submit
                }

                zip(
                    arrayListOf(confusingSrc.path, resources.path, lib.path),
                    project?.basePath + File.separator + "build-output" + File.separator + "${name}.zip"
                )
                VertxServer.Command.saveProject(zip.canonicalPath)
                VertxServer.Command.runProject(zip.canonicalPath)
//                zip.delete()
                logI("项目正在上传: " + projectJson.path)
            }
            return
        }
        logE("项目无法上传: 选择的文件夹没有包含项目描述文件 'project.json'")
    }
}