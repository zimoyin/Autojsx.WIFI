package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Separator

class AutoMenu : ActionGroup() {
    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        return arrayOf<AnAction>(
            RunScript(), RunProject(), Separator(),
            SaveAndRunProject(), SaveAndRunScript(), Separator(),
            SaveProject(), SaveDir(), SaveScript(), Separator(),
            StopRunning(), StopAll(), Separator(),
            StartServer(), StopServer(), Separator(),
            SelectDevice(),Separator(),
            Screenshot(),AnalysisPageNode(),AnalysisApplicationList(),
            Separator(),ADBTODO()
        )
    }

}