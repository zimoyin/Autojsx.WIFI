package github.zimo.autojsx.action.save

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import github.zimo.autojsx.server.ConsoleOutputV2
import github.zimo.autojsx.server.VertxServer
import github.zimo.autojsx.util.caseString
import github.zimo.autojsx.util.logI
import github.zimo.autojsx.util.runServer

class SaveDoc : AnAction(github.zimo.autojsx.icons.ICONS.SAVE_FILE_16) {

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        if (file.isDirectory) return
        val project = e.project
        runServer(project)
        //TODO 创建临时混淆目录，并混淆，如果开启了混淆
        //TODO 抽取公共方法： 保存脚本/项目，运行脚本/项目....
        runCatching {
            VertxServer.Command.saveJS(file.path)
            logI("js 文件保存成功: ${file.path}")
        }.onFailure {
            ConsoleOutputV2.systemPrint("js脚本网络引擎执行失败${file.path} /E \r\n" + it.caseString())
        }
    }
}