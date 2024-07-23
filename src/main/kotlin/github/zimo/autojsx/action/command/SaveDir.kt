package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.server.VertxCommandServer
import github.zimo.autojsx.util.executor
import github.zimo.autojsx.util.logI
import github.zimo.autojsx.util.runServer
import github.zimo.autojsx.util.zip
import java.io.File

class SaveDir :
    AnAction("上传文件夹", "保存文件夹", github.zimo.autojsx.icons.ICONS.SAVE_16) {
    override fun actionPerformed(e: AnActionEvent) {
        val dialog = FileChooserFactory.getInstance()
            .createFileChooser(FileChooserDescriptorFactory.createSingleFolderDescriptor(), e.project, null)

        val files = dialog.choose(e.project)
//            val selectedDir = files[0]

        if (files.isNotEmpty()) {
            val dir = files[0]
            runProject(dir, e.project)
        }
    }

    private fun runProject(file: VirtualFile, project: Project?) {
        runServer(project)
        val zip = File(project?.basePath + "/build-output" + "/${file.name}.zip")
        zip.parentFile.mkdirs()
        if (zip.exists()) zip.delete()

        executor.submit {
            zip(
                arrayListOf(file.path),
                project?.basePath + File.separator + "build-output" + File.separator + "${file.name}.zip"
            )
            VertxCommandServer.Command.saveProject(zip.canonicalPath)
            VertxCommandServer.Command.runProject(zip.canonicalPath)
//            zip.delete()
            logI("文件夹正在上传: " + file.path)
        }
    }
}