package github.zimo.autojsx.action.run.top

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.modules
import com.jetbrains.rd.util.getLogger
import com.jetbrains.rd.util.warn
import github.zimo.autojsx.action.news.NewAutoJSX
import github.zimo.autojsx.icons.ICONS
import github.zimo.autojsx.module.MODULE_TYPE_ID
import github.zimo.autojsx.server.ConsoleOutputV2
import github.zimo.autojsx.server.VertxCommandServer


/**
 * 停止所有运行
 */
class StopAllButton:
    AnAction(ICONS.STOP_16) {
    override fun actionPerformed(e: AnActionEvent) {
        VertxCommandServer.Command.stopAll()
        ConsoleOutputV2.systemPrint("Action/I: 停止所有脚本指令已发送")
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val moduleManager = ModuleManager.getInstance(project)
            val hasAutoJSXModule = moduleManager.modules.any { module ->
                ModuleType.get(module).id == MODULE_TYPE_ID
            }
            e.presentation.isEnabledAndVisible = hasAutoJSXModule
        } else {
            e.presentation.isEnabledAndVisible = false
        }
    }
}