package github.zimo.autojsx.action.run.dir

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.icons.ICONS
import github.zimo.autojsx.module.MODULE_TYPE_ID
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.util.*
import github.zimo.autojsx.window.AutojsxConsoleWindow
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
        val folder = e.getData(PlatformDataKeys.VIRTUAL_FILE)

        AutojsxConsoleWindow.show(project)
        runServer(project)
        if (folder?.isDirectory == true) {
            zipProject(folder, e.project).apply {
                logI("预运行项目: " + info.projectJson)
                logI("├──> 项目 src: " + info.src?.canonicalPath)
                logI("├──> 项目 resources: " + info.resources?.canonicalPath)
                logI("└──> 项目 lib: " + info.lib?.canonicalPath + "\r\n")
                if (info.src == null){
                    logE("无法运行该项目，该文件夹不是一个项目文件夹: src 为空")
                    return
                }
                VertxCommand.runProject(bytes, info.name)
            }
        }
    }
    override fun update(e: AnActionEvent) {
        // TODO "The update method used a method marked as unstable"
        e.presentation.isEnabledAndVisible = (e.project?.modules?.count { it.moduleTypeName == MODULE_TYPE_ID } ?: 0) > 0
    }
}