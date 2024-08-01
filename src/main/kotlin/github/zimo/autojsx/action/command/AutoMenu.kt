package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Separator

class AutoMenu : ActionGroup() {
    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        return arrayOf(
            /**/
            RunScript(), RunProject(), Separator(),
            /**/
            SaveAndRunProject(), SaveAndRunScript(), Separator(),
            /**/
            SaveCurrentProject(), SaveProject(), SaveDir(), SaveScript(), Separator(),
            /**/
            StopRunning(), StopAll(), Separator(),
            /**/
            StartServer(), StopServer(),ModifyServerIpAddress(), ModifyServerPort(), Separator(),
            /**/
            SelectDevice(), Separator(),
            /**/
            Screenshot(), AnalysisPageNode(), AnalysisApplicationList(),
            /**/
//            Separator(), ADBTODO()
        )
    }

}