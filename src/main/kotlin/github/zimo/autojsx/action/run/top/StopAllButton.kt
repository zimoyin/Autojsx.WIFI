package github.zimo.autojsx.action.run.top

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.modules
import com.jetbrains.rd.util.getLogger
import com.jetbrains.rd.util.warn
import github.zimo.autojsx.action.news.NewAutoJSX
import github.zimo.autojsx.icons.ICONS
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
        getLogger<NewAutoJSX>().warn { "The update method used a method marked as unstable" }
        e.presentation.isEnabledAndVisible = (e.project?.modules?.count { it.moduleTypeName == "AUTO_JSX_MODULE_TYPE" } ?: 0) > 0
    }
}