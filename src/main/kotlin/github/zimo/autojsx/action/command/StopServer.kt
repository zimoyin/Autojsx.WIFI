package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


class StopServer :
    AnAction("停启服务器","停启服务器",github.zimo.autojsx.icons.ICONS.STOP_SERVER_16) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}