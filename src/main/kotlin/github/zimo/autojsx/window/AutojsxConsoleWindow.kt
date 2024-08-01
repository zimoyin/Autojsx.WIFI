package github.zimo.autojsx.window

import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.externalSystem.ExternalSystemConfigurableAware
import com.intellij.openapi.externalSystem.service.settings.AbstractExternalSystemConfigurable
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.panel
import github.zimo.autojsx.icons.ICONS
import github.zimo.autojsx.server.ConsoleOutput
import github.zimo.autojsx.server.Devices
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.util.*
import org.jetbrains.plugins.gradle.service.project.open.GradleProjectOpenProcessor
import org.jetbrains.plugins.gradle.service.project.open.canOpenGradleProject
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.ListCellRenderer


class AutojsxConsoleWindow : ToolWindowFactory {
    companion object {
        fun show(project: Project?) {
            if (project == null) return
            val toolWindowManager = ToolWindowManager.getInstance(project)
            val toolWindow = toolWindowManager.getToolWindow("AutojsxConsole")

            if (toolWindow != null) {
                toolWindow.show(null)
            } else {
                // 处理工具窗口未找到的情况
                logW("工具窗口 'Autojsx Console' 未找到")
            }
        }
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {

        val comboBoxRenderer: ListCellRenderer<Any?>? = null // 如果你需要自定义渲染器，可以在这里传入相应的实现，否则可以保持为null

        //IDEA内部控制台
        val consoleByIDEA = consoleByIDEA(comboBoxRenderer, project)

        // ----------------------------------------------------------------
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(consoleByIDEA, "", false)

        // Add the content to the tool window
        toolWindow.contentManager.addContent(content)
    }

    private fun consoleByIDEA(
        comboBoxRenderer: ListCellRenderer<Any?>?,
        project: Project,
    ): JPanel {
        // 创建上下布局的主面板
        val contentPanel = JPanel()
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)


        // 创建上面的JPanel
        val topPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        topPanel.maximumSize = Dimension(Int.MAX_VALUE, 30)
        val top = panel {
            indent {
                row {
                    label("输出的设备")
                    comboBox(Devices, comboBoxRenderer).apply {
                        Devices.component = component
                        Devices.currentDevice = component.item
                        component.addActionListener {
                            if (ConsoleOutput.currentDevice != component.item && component.item != null) {
                                Devices.currentDevice = component.item
                                ConsoleOutput.update()
                            }
                        }
                    }
                    label("日志等级")
                    comboBox(arrayListOf("V", "D", "I", "W", "E"), comboBoxRenderer).apply {
                        ConsoleOutput.setLevel(component.item)
                        component.addActionListener {
                            if (ConsoleOutput.level0 != component.item && component.item != null) {
                                ConsoleOutput.setLevel(component.item)
                                ConsoleOutput.update()
                            }
                        }
                    }

                    button("清空日志") {
                        ConsoleOutput.clear()
                    }

                    button("回到底部") {
                        ConsoleOutput.toEnd()
                    }
                    button("开启服务器") {
                        runServer(project)
                    }.apply {
                        component.icon = ICONS.START_SERVER_16
                    }
                    button("关闭服务器") {
                        stopServer(project)
                    }.apply {
                        component.icon = ICONS.STOP_SERVER_16
                    }
                    button("选择设备列表") {
                        selectDevice()
                    }
                    button("选择需要关闭的脚本") {
                        //TODO 无法关闭脚本，测试示例为死循环 while
                        runningScriptList(project)
                    }
                    button("运行项目") {
                        runServer(project)
                        searchProjectJsonByEditor(project) { file ->
                            zipProject(file, project).apply {
                                logI("预运行项目: " + info.projectJson)
                                logI("├──> 项目 src: " + info.src?.canonicalPath)
                                logI("├──> 项目 resources: " + info.resources?.canonicalPath)
                                logI("└──> 项目 lib: " + info.lib?.canonicalPath + "\r\n")
                                VertxCommand.runProject(bytes, info.name)
                            }
                        }
                    }.apply {
                        component.icon = ICONS.START_16
                    }
                    button("停止所有项目") {
                        VertxCommand.stopAll()
                        ConsoleOutput.systemPrint("Action/I: 停止所有脚本指令已发送")
                    }.apply {
                        component.icon = ICONS.STOP_16
                    }
                }
            }
        }
        topPanel.add(top)

        // 创建控制台
        val consoleView = ConsoleViewImpl(project, true)
        ConsoleOutput.console = consoleView
        ConsoleOutput.isInitOutput = false
        consoleView.print("欢迎使用 Autojsx!", ConsoleViewContentType.LOG_DEBUG_OUTPUT)
        consoleView.performWhenNoDeferredOutput {
            ConsoleOutput.console!!.flushDeferredText()
        }

        // 创建上面的按钮区
        contentPanel.add(topPanel)

        // 创建下面的JPanel并使用Box.Filler填充剩余空间
        contentPanel.add(consoleView)

        // 使用Box.Filler填充剩余空间
        contentPanel.add(Box.createVerticalGlue())
        return contentPanel
    }
}
