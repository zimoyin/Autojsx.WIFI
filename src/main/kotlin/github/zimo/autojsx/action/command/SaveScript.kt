package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * 保存当前文件到app 的根路径，注意保存的也许是二进制文件
 */
class SaveScript :
    AnAction("保存当前文件","保存文件",github.zimo.autojsx.icons.ICONS.SAVE_FILE_16) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}