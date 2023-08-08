package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class SaveProject :
    AnAction("保存当前项目","保存项目",github.zimo.autojsx.icons.ICONS.SAVE_16) {
    override fun actionPerformed(e: AnActionEvent) {
        //TODO 选择文件夹界面
        println("test1")
    }
}