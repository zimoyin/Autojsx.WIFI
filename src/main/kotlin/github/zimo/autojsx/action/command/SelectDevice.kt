package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.util.selectDevice


/**
 * 选择设备后，任何操作只对当前设备生效。默认是所有设备
 */
class SelectDevice :
    AnAction("选择设备", "选择设备", github.zimo.autojsx.icons.ICONS.LOGO_16) {
    override fun actionPerformed(e: AnActionEvent) {
        selectDevice()
    }
}