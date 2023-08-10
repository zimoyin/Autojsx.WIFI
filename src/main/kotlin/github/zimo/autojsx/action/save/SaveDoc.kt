package github.zimo.autojsx.action.save

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import github.zimo.autojsx.server.ConsoleOutputV2
import github.zimo.autojsx.server.VertxServer
import github.zimo.autojsx.util.caseString
import github.zimo.autojsx.util.runServer

class SaveDoc : AnAction(github.zimo.autojsx.icons.ICONS.SAVE_FILE_16) {

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        if (file.isDirectory) return
        val project = e.project
        runServer(project)
        runCatching {
            VertxServer.Command.saveJS(file.path)
        }.onFailure {
            ConsoleOutputV2.systemPrint("js脚本网络引擎执行失败${file.path} /E \r\n" + it.caseString())
        }
    }
}