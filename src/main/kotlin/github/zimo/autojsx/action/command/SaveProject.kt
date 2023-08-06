package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.module.MyModuleType
import javax.swing.ImageIcon

class SaveProject :
    AnAction("保存当前项目","保存项目",github.zimo.autojsx.icons.ICONS.LOGO) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}