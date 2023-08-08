package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * 选择设备后，任何操作只对当前设备生效。默认是所有设备
 */
class SelectDevice :
    AnAction("选择设备","不保存文件运行",github.zimo.autojsx.icons.ICONS.LOGO_16) {
    override fun actionPerformed(e: AnActionEvent) {
        //TODO 选择界面
        println("test1")
    }
}