package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


class AnalysisPageNode :
    AnAction("分析页面节点","分析页面节点",github.zimo.autojsx.icons.ICONS.LOGO_16) {
    override fun actionPerformed(e: AnActionEvent) {
        println("test1")
    }
}