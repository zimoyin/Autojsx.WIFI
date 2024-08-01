package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import github.zimo.autojsx.server.VertxServer

/**
 * 选择设备后，任何操作只对当前设备生效。默认是所有设备
 */
class ModifyServerPort :
    AnAction("修改服务器端口", "", null) {
    override fun actionPerformed(e: AnActionEvent) {
        Messages.showInputDialog(e.project, "请输入服务器端口:", "修改端口", Messages.getQuestionIcon())?.trim()?.let {
            runCatching {
                VertxServer.port = it.toInt()
            }
        }
    }
}