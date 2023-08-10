package github.zimo.autojsx.action.run.top

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.icons.ICONS
import github.zimo.autojsx.server.ConsoleOutputV2
import github.zimo.autojsx.server.VertxServer


/**
 * 停止所有运行
 */
class StopAllButton:
    AnAction(ICONS.STOP_16) {
    override fun actionPerformed(e: AnActionEvent) {
        VertxServer.Command.stopAll()
        ConsoleOutputV2.systemPrint("Action/I: 停止所有脚本指令已发送")
    }
}