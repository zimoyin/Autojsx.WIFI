package github.zimo.autojsx.action.run.top

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.modules
import github.zimo.autojsx.icons.ICONS
import github.zimo.autojsx.module.MODULE_TYPE_ID
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.util.*
import github.zimo.autojsx.window.AutojsxConsoleWindow


/**
 * 运行当前项目
 */
class TopRunButton : AnAction(ICONS.START_16) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project!!
        AutojsxConsoleWindow.show(project)
        runServer(project)

        if (GradleUtils.isGradleProject(project)) {
            GradleUtils.runGradleCommandOnToolWindow(project, "compile") {
                if (it) {
                    logI(getGradleOutputMainJsPath(project))
                    val zip = zipProject(getGradleOutputMainJsPath(project), project)
                    VertxCommand.runProject(zip.bytes, zip.info.name)
                } else {
                    logE("Gradle:buildMainJs 构建失败,请通过构建窗口查看错误信息")
                }
            }
        } else {
            // 运行项目
            runCatching {
                searchProjectJsonByEditor(project) { file ->
                    zipProject(file, e.project).apply {
                        logI("预运行项目: " + info.projectJson)
                        logI("├──> 项目 src: " + info.src?.canonicalPath)
                        logI("├──> 项目 resources: " + info.resources?.canonicalPath)
                        logI("└──> 项目 lib: " + info.lib?.canonicalPath + "\r\n")
                        VertxCommand.runProject(bytes, info.name)
                    }
                }
            }.onFailure {
                logE("js脚本网络引擎执行失败 ", it)
            }
        }
    }


    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = (e.project?.modules?.count { ModuleType.get(it).id == MODULE_TYPE_ID } ?: 0) > 0
    }
}