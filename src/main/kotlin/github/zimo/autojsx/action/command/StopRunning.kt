package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressManager
import github.zimo.autojsx.server.VertxServer
import github.zimo.autojsx.util.executor
import github.zimo.autojsx.util.runningList
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JOptionPane
import javax.swing.JPanel


/**
 * 不一定能实现
 */
class StopRunning :
    AnAction("停止运行", "选择脚本进行停止运行", github.zimo.autojsx.icons.ICONS.STOP_16) {
    override fun actionPerformed(e: AnActionEvent) {
        runningList(e)
    }


}