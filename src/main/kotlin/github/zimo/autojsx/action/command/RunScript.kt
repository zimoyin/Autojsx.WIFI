package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class RunScript :
    AnAction("运行当前脚本","不保存文件运行",github.zimo.autojsx.icons.ICONS.START_16) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}