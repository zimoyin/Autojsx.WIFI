package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


class StopAll :
    AnAction("停止所有脚本","停止运行",github.zimo.autojsx.icons.ICONS.STOP_16) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}