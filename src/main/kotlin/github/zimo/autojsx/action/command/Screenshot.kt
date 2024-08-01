package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.util.base64_image_toFile
import github.zimo.autojsx.util.executor
import github.zimo.autojsx.util.logI
import github.zimo.autojsx.util.logW


/**
 * 截图
 */
class Screenshot :
    AnAction("截图", "截图", github.zimo.autojsx.icons.ICONS.LOGO_16) {
    override fun actionPerformed(e: AnActionEvent) {
        executor.submit {
            logI("正在截图")
            val times = System.currentTimeMillis()
            VertxCommand.getScreenshot({
                if (it.isEmpty()) {
                    logW("截图失败没有获取屏幕截图 base64")
                    return@getScreenshot
                }
                val file = base64_image_toFile(it, e.project?.basePath ?: "", times)
                logI("截图完成: $file")
            })
        }
    }


}