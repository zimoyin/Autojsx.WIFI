package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class ConfusingAndSaveProject :
    AnAction("混淆并保存项目","混淆并保存项目",github.zimo.autojsx.icons.ICONS.SAVE_16) {
    override fun actionPerformed(e: AnActionEvent) {
        //TODO 选择文件夹界面
        println("test1")
    }
}