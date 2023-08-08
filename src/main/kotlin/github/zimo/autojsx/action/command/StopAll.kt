package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.server.ConsoleOutputV2
import github.zimo.autojsx.server.VertxServer


class StopAll :
    AnAction("停止所有脚本","停止运行",github.zimo.autojsx.icons.ICONS.STOP_16) {
    override fun actionPerformed(e: AnActionEvent) {
        VertxServer.Command.stopAll()
        ConsoleOutputV2.systemPrint("Action/I: 停止所有脚本指令已发送")
    }
}