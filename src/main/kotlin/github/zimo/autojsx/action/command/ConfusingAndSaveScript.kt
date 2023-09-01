package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

@Deprecated("Please use")
class ConfusingAndSaveScript :
    AnAction("混淆并保存脚本","混淆并保存脚本",github.zimo.autojsx.icons.ICONS.SAVE_16) {
    override fun actionPerformed(e: AnActionEvent) {
        //TODO 选择文件夹界面
        println("test1")
    }
}