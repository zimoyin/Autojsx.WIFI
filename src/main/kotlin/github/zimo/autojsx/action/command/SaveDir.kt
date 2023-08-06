package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class SaveDir :
    AnAction("保存当前文件夹","保存文件夹",github.zimo.autojsx.icons.ICONS.SAVE_16) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}