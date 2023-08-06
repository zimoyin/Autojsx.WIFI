package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.module.MyModuleType
import javax.swing.ImageIcon

/**
 * 保存当前文件到app 的根路径，注意保存的也许是二进制文件
 */
class SaveScript :
    AnAction("保存当前文件","保存文件",ImageIcon(MyModuleType::class.java.classLoader.getResource("icons/pluginMainDeclaration.png"))) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}