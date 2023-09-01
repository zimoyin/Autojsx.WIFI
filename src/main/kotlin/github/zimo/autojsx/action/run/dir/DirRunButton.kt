package github.zimo.autojsx.action.run.dir

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.icons.ICONS
import github.zimo.autojsx.server.VertxServer
import github.zimo.autojsx.util.*
import io.vertx.core.json.JsonObject
import java.io.File


/**
 * 运行当前项目
 */
class DirRunButton : AnAction(ICONS.START_16) {

    private val projectJSON = "project.json"
    private val targetFileName = "project.json"
    private val targetDirName = "resources"
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        runServer(project)
        val folder = e.getData(PlatformDataKeys.VIRTUAL_FILE)
        if (folder?.isDirectory == true) {
            runProject(folder, e.project)
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
            zip.parentFile.mkdirs()
            if (zip.exists()) zip.delete()

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
//                if (zip.exists()) zip.delete()

            }
            return
        }
        logE("项目无法上传: 选择的文件夹没有包含项目描述文件 'project.json'")
    }
}