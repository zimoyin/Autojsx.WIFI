package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class SaveAndRunScript :
    AnAction("保存并运行当前脚本","保存文件运行",github.zimo.autojsx.icons.ICONS.SAVE_RUN_16) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}