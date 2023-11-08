package github.zimo.autojsx.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.*
import github.zimo.autojsx.window.BrowserWindow.Const.browser
import java.awt.BorderLayout
import java.nio.charset.StandardCharsets
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities


class BrowserWindow : ToolWindowFactory {
    object Const {
        val browser = JBCefBrowser().apply {
            setProperty(JBCefClient.Properties.JS_QUERY_POOL_SIZE, "32")
        }
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
//        browser.loadURL("https://baidu.com")
        val bytes =
            BrowserWindow::class.java.classLoader.getResourceAsStream("obfuscation/index.html")?.readAllBytes()?:ByteArray(0)
        browser.loadHTML(String(bytes, StandardCharsets.UTF_8))


        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(Const.browser.component, null, false)
        // Add the content to the tool window
        toolWindow.contentManager.addContent(content)
    }


    fun open() {
        SwingUtilities.invokeLater {
            val jbCefBrowser = JBCefBrowser()
            val frame = JFrame("BrowserWindow")
            frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE

            // 创建内嵌浏览器
            jbCefBrowser.loadURL("https://baidu.com")

            // 创建一个面板，将内嵌浏览器添加到其中
            val panel = JPanel(BorderLayout())
            panel.add(jbCefBrowser.component, BorderLayout.CENTER)

            frame.add(panel)
            frame.setSize(800, 600)
            frame.isVisible = true
        }
    }
}
