package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import github.zimo.autojsx.server.VertxServer
import github.zimo.autojsx.util.executor
import github.zimo.autojsx.util.logI
import github.zimo.autojsx.util.logW
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO


/**
 * 截图
 */
class Screenshot :
    AnAction("截图", "截图", github.zimo.autojsx.icons.ICONS.LOGO_16) {
    override fun actionPerformed(e: AnActionEvent) {
        executor.submit {
            logI("正在截图")
            val times = System.currentTimeMillis()
            VertxServer.Command.getScreenshot({
                if (it.isEmpty()){
                    logW("截图失败没有获取屏幕截图 base64")
                    return@getScreenshot
                }
                val bytes = Base64.getDecoder().decode(it)
                val image = ImageIO.read(ByteArrayInputStream(bytes))
                val file = File(e.project?.basePath+File.separator+"/build-output/images/$times/${UUID.randomUUID()}.png")
                file.parentFile.mkdirs()
                ImageIO.write(image, "png", file)
                logI("截图完成: $file")
            })
        }
    }
}