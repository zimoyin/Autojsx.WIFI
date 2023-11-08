package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.window.BrowserWindow

class Confusing :
    AnAction("混淆工具设置", "混淆工具设置", github.zimo.autojsx.icons.ICONS.SAVE_16) {
    override fun actionPerformed(e: AnActionEvent) {
//        BrowserUtil.browse("https://obfuscator.io/")
        BrowserWindow().open()
//        logE("待实现")
        TODO()
        //TODO : 在这里设置当项目运行或者保存时自动混淆
        //TODO: 在这里设置项目混淆级别和自定义混淆
    }
}