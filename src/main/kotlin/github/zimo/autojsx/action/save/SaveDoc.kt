package github.zimo.autojsx.action.save

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import github.zimo.autojsx.server.ConsoleOutput
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.util.caseString
import github.zimo.autojsx.util.logI
import github.zimo.autojsx.util.runServer

class SaveDoc : AnAction(github.zimo.autojsx.icons.ICONS.SAVE_FILE_16) {

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        if (file.isDirectory) return
        val project = e.project
        runServer(project)
        //TODO 抽取公共方法： 保存脚本/项目，运行脚本/项目....
        runCatching {
            VertxCommand.saveJS(file.path)
            logI("js 文件保存成功: ${file.path}")
        }.onFailure {
            ConsoleOutput.systemPrint("js脚本网络引擎执行失败${file.path} /E \r\n" + it.caseString())
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val isJsFile = file != null && "js" == file.extension
        e.presentation.isEnabledAndVisible = isJsFile
    }
}