package github.zimo.autojsx.action.run.doc

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.module.MyModuleType
import javax.swing.ImageIcon

/**
 * 运行当前脚本
 */
class DocRunButton:
    AnAction(ImageIcon(MyModuleType::class.java.classLoader.getResource("icons/pluginMainDeclaration.png"))) {
    override fun actionPerformed(e: AnActionEvent) {
        TODO("Not yet implemented")
    }
}