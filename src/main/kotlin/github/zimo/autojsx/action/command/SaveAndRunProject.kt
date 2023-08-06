package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.module.MyModuleType
import javax.swing.ImageIcon

class SaveAndRunProject :
    AnAction("保存并运行项目","打包项目到APP并运行",github.zimo.autojsx.icons.ICONS.LOGO) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}