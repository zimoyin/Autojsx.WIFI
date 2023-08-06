package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.module.MyModuleType
import javax.swing.ImageIcon

/**
 * 不一定能实现
 */
class StopRunning :
    AnAction("停止运行","选择一个进行停止运行",github.zimo.autojsx.icons.ICONS.LOGO) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}