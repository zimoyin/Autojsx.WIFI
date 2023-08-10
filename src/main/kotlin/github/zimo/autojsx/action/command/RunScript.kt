package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import github.zimo.autojsx.server.VertxServer
import github.zimo.autojsx.util.logE
import github.zimo.autojsx.util.runServer

class RunScript :
    AnAction("选择运行脚本","不保存文件运行",github.zimo.autojsx.icons.ICONS.START_16) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        runServer(project)
        val dialog = FileChooserFactory.getInstance()
            .createFileChooser(FileChooserDescriptorFactory.createSingleFileDescriptor(), e.project, null)

        val files = dialog.choose(e.project)
//            val selectedDir = files[0]

        if (files.isNotEmpty()) {
            val file = files[0]
            runCatching {
                VertxServer.Command.rerunJS(file.path)
            }.onFailure {
               logE("js脚本网络引擎执行失败${file.path} ",it)
            }
        }
    }
}