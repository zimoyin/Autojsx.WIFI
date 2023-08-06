package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.module.MyModuleType
import javax.swing.ImageIcon


class StopServer :
    AnAction("停启服务器","停启服务器",github.zimo.autojsx.icons.ICONS.LOGO) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}