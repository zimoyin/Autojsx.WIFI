package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class SaveAndRunProject :
    AnAction("保存并运行项目","打包项目到APP并运行",github.zimo.autojsx.icons.ICONS.SAVE_RUN_16) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}