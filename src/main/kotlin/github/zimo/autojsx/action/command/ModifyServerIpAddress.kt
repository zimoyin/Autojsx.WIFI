package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import github.zimo.autojsx.server.VertxServer

/**
 * 修改服务器对外显示IP
 */
class ModifyServerIpAddress :
    AnAction("修改服务器IP", "", null) {
    override fun actionPerformed(e: AnActionEvent) {
        Messages.showInputDialog(e.project, "请输入服务器IP:", "修改IP", Messages.getQuestionIcon())?.trim()?.let {
            runCatching {
                VertxServer.ipAddress = it
            }
        }
    }
}