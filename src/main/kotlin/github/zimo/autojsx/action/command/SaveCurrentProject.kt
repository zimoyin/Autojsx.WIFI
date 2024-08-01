package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.util.*
import io.vertx.core.json.JsonObject
import java.io.File

class SaveCurrentProject :
    AnAction("保存当前项目", "保存当前项目", github.zimo.autojsx.icons.ICONS.SAVE_RUN_16) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        searchProjectJsonByEditor(project) { file ->
            zipProject(file, e.project).apply {
                logI("预运行项目: " + info.projectJson)
                logI("├──> 项目 src: " + info.src?.canonicalPath)
                logI("├──> 项目 resources: " + info.resources?.canonicalPath)
                logI("└──> 项目 lib: " + info.lib?.canonicalPath + "\r\n")
                if (info.src == null) {
                    logE("无法运行该项目，该文件夹不是一个项目文件夹: src 为空")
                    return@apply
                }
                VertxCommand.saveProject(bytes, info.name)
            }
        }
    }
}