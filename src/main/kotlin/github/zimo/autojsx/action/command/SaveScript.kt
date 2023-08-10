package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import github.zimo.autojsx.server.VertxServer
import github.zimo.autojsx.util.logE
import github.zimo.autojsx.util.runServer

/**
 * 保存当前文件到app 的根路径，注意保存的也许是二进制文件
 */
class SaveScript :
    AnAction("上传脚本/文本文件", "保存文件", github.zimo.autojsx.icons.ICONS.SAVE_FILE_16) {
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
                VertxServer.Command.saveJS(file.path)
            }.onFailure {
                logE("js脚本网络引擎执行失败${file.path} ",it)
            }
        }
    }
}