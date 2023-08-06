package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.module.MyModuleType
import javax.swing.ImageIcon

class SaveAndRunScript :
    AnAction("保存并运行当前脚本","保存文件运行",ImageIcon(MyModuleType::class.java.classLoader.getResource("icons/pluginMainDeclaration.png"))) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}