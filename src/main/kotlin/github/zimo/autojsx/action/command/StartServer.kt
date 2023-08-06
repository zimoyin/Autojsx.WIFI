package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


class StartServer :
    AnAction("开启服务器","开启服务器",github.zimo.autojsx.icons.ICONS.START_SERVER_16) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}