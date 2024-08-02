package github.zimo.autojsx.action.run.top

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.modules
import github.zimo.autojsx.icons.ICONS
import github.zimo.autojsx.module.MODULE_TYPE_ID
import github.zimo.autojsx.server.ConsoleOutput
import github.zimo.autojsx.server.VertxCommand


/**
 * 停止所有运行
 */
class StopAllButton:
    AnAction(ICONS.STOP_16) {
    override fun actionPerformed(e: AnActionEvent) {
        VertxCommand.stopAll()
        ConsoleOutput.systemPrint("Action/I: 停止所有脚本指令已发送")
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = (e.project?.modules?.count { ModuleType.get(it).id == MODULE_TYPE_ID } ?: 0) > 0
    }
}