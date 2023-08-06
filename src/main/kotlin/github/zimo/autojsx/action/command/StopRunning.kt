package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * 不一定能实现
 */
class StopRunning :
    AnAction("停止运行","选择一个进行停止运行",github.zimo.autojsx.icons.ICONS.STOP_16) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}