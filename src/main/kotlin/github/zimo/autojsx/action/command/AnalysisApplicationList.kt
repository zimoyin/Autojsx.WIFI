package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.module.MyModuleType
import javax.swing.ImageIcon

/**
 * 1. 当前应用
 * 2. 安装的其他应用
 */
class AnalysisApplicationList :
    AnAction("分析应用列表","获取手机安装的应用信息等",ImageIcon(MyModuleType::class.java.classLoader.getResource("icons/pluginMainDeclaration.png"))) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}