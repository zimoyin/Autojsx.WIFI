package github.zimo.autojsx.action.run.top

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.module.MyModuleType
import javax.swing.ImageIcon

/**
 * 停止所有运行
 */
class StopAllButton:
    AnAction(ImageIcon(MyModuleType::class.java.classLoader.getResource("icons/pluginMainDeclaration.png"))) {
    override fun actionPerformed(e: AnActionEvent) {
        TODO("Not yet implemented")
    }
}