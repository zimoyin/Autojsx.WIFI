package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.util.runServer


class StartServer :
    AnAction("开启服务器","开启服务器",github.zimo.autojsx.icons.ICONS.START_SERVER_16) {
    override fun actionPerformed(e: AnActionEvent) {
        runServer(e.project)
    }
}