package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.server.VertxServer
import github.zimo.autojsx.util.logE
import github.zimo.autojsx.util.logI
import github.zimo.autojsx.util.logW
import java.io.File


class AnalysisPageNode :
    AnAction("页面节点获取","页面节点转xml",github.zimo.autojsx.icons.ICONS.LOGO_16) {
    override fun actionPerformed(e: AnActionEvent) {
        if (!VertxServer.isStart || VertxServer.selectDevicesWs.isEmpty()) {
            logW("服务器中未选中设备")
            return
        }
        VertxServer.Command.getNodes({
            val zip = File(e.project?.basePath + "/build-output" + "/node/${System.currentTimeMillis()}.xml")
            zip.parentFile.mkdirs()
            zip.writeText(it)
            logI("保存节点文件: ${zip.canonicalPath}")
        })
    }
}