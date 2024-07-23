package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.server.VertxCommandServer
import github.zimo.autojsx.util.logE
import github.zimo.autojsx.util.logI
import github.zimo.autojsx.util.logW
import java.io.File


class AnalysisPageNode :
    AnAction("页面节点获取","页面节点转xml",github.zimo.autojsx.icons.ICONS.LOGO_16) {
    override fun actionPerformed(e: AnActionEvent) {
        if (!VertxCommandServer.isStart || VertxCommandServer.selectDevicesWs.isEmpty()) {
            logW("服务器中未选中设备")
            return
        }
        VertxCommandServer.Command.getNodes({
            val zip = File(e.project?.basePath + "/build-output" + "/node/${System.currentTimeMillis()}.xml")
            zip.parentFile.mkdirs()
            zip.writeText(it)
            if (it.trim().isEmpty()){
                logE("无法获取到节点,请查看手机是否提示授权,或者提示开启无障碍模式/服务")
            }else{
                logI("保存节点文件: ${zip.canonicalPath}")
            }
        })
    }
}