package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.util.*
import io.vertx.core.json.JsonObject
import java.io.File


class RunProject :
    AnAction("选择运行项目", "不保存项目运行", github.zimo.autojsx.icons.ICONS.START_16) {
    private val projectJSON = "project.json"
    override fun actionPerformed(e: AnActionEvent) {
        val dialog = FileChooserFactory.getInstance()
            .createFileChooser(FileChooserDescriptorFactory.createSingleFolderDescriptor(), e.project, null)

        val files = dialog.choose(e.project)

        if (files.isNotEmpty()) {
            zipProject(files.last(), e.project).apply {
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
}