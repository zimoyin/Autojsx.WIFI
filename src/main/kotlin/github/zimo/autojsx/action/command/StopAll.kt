package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.module.MyModuleType
import javax.swing.ImageIcon


class StopAll :
    AnAction("停止所有脚本","停止运行",github.zimo.autojsx.icons.ICONS.LOGO) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}