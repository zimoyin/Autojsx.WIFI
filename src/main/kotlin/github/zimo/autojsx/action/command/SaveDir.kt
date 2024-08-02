package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.util.logI
import github.zimo.autojsx.util.zipProject

class SaveDir :
    AnAction("上传文件夹", "保存文件夹", github.zimo.autojsx.icons.ICONS.SAVE_16) {
    override fun actionPerformed(e: AnActionEvent) {
        val dialog = FileChooserFactory.getInstance()
            .createFileChooser(FileChooserDescriptorFactory.createSingleFolderDescriptor(), e.project, null)

        val files = dialog.choose(e.project)
//            val selectedDir = files[0]

        if (files.isNotEmpty()) {
            val dir = files[0]
            zipProject(dir,e.project).apply{
                logI("文件夹正在上传: $dir")
                VertxCommand.saveProject(bytes,dir.path)
            }
        }
    }
}