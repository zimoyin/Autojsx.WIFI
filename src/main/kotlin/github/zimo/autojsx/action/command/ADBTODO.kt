package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class ADBTODO :
    AnAction("ADB TODO","不保存项目运行",github.zimo.autojsx.icons.ICONS.LOGO_16) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}