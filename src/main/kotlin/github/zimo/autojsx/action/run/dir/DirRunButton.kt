package github.zimo.autojsx.action.run.dir

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.icons.ICONS
import github.zimo.autojsx.module.MODULE_TYPE_ID
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.util.*
import github.zimo.autojsx.window.AutojsxConsoleWindow


/**
 * 运行当前项目
 */
class DirRunButton : AnAction(ICONS.START_16) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val folder = e.getData(PlatformDataKeys.VIRTUAL_FILE)

        var findFile: VirtualFile? = folder
        // 向上查询 src/resources 目录的父目录
        for (i in 0 until 6) {
            if (findFile == null) break
            if (findFile.parent == null || findFile.parent.canonicalPath == project!!.basePath) {
                break
            }
            findFile = findFile.parent
            if (findFile.findDirectory("src") != null || findFile.findDirectory("resources") != null) {
                break
            }
        }
        findFile = findFile?.let {
            if (it.findDirectory("src") != null || it.findDirectory("resources") != null) it else folder
        }

        val issrc = findFile?.findDirectory("src") != null && findFile.findDirectory("resources") != null

        AutojsxConsoleWindow.show(project)
        runServer(project)
        executor.execute {
            if (findFile?.isDirectory == true) {
                if (issrc) {
                    zipProject(findFile, e.project, 3).apply {
                        logI("预运行项目: " + info.projectJson)
                        logI("├──> 项目 src: " + info.src?.canonicalPath)
                        logI("├──> 项目 resources: " + info.resources?.canonicalPath)
                        logI("└──> 项目 lib: " + info.lib?.canonicalPath + "\r\n")
                        if (info.src == null) {
                            logW("project.json 文件不存在或者不存在 srcPath 字段")
                            runGradle(project)
                        } else {
                            VertxCommand.runProject(bytes, info.name)
                        }
                    }
                } else {
                    runGradle(project)
                }

            }
        }
    }

    private fun runGradle(project: Project?) {
        if (GradleUtils.isGradleProject(project!!)) {
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "正在构建项目", true) {
                override fun run(indicator: ProgressIndicator) {
                    logI("开始构建 Gradle:compile")
                    GradleUtils.runGradleCommand(project, "compile") { result ->
                        if (result.success) {
                            logI(getGradleOutputMainJsPath(project))
                            val zip = zipProject(getGradleOutputMainJsPath(project), project)
                            VertxCommand.runProject(zip.bytes, zip.info.name)
                        } else {
                            logE("Gradle:compile 构建失败\n${result.error}\n")
                        }
                    }
                }
            })
        } else {
            logE("无法运行该项目，该文件夹不是一个项目文件夹: src 为空 && 未能检测到 Gradle")
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible =
            (e.project?.modules?.count { ModuleType.get(it).id == MODULE_TYPE_ID } ?: 0) > 0
    }
}