package github.zimo.autojsx.window

import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.panel
import github.zimo.autojsx.icons.ICONS
import github.zimo.autojsx.server.ConsoleOutputV2
import github.zimo.autojsx.server.ConsoleOutput_V1
import github.zimo.autojsx.server.Devices
import github.zimo.autojsx.server.VertxServer
import github.zimo.autojsx.util.runServer
import github.zimo.autojsx.util.selectDevice
import github.zimo.autojsx.util.stopServer
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*


class AutojsxConsoleWindow : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {

        val comboBoxRenderer: ListCellRenderer<Any?>? = null // 如果你需要自定义渲染器，可以在这里传入相应的实现，否则可以保持为null

        //自实现TextArea控制台
        val consoleByTextArea = consoleByTextArea(comboBoxRenderer)
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
        contentPanel.add(topPanel)
        val top = panel {
            indent {
                row {
                    label("输出的设备")
                    comboBox(Devices, comboBoxRenderer).apply {
                        Devices.component = component
                        Devices.currentDevice = component.item
                        component.addActionListener {
                            if (ConsoleOutputV2.currentDevice != component.item && component.item != null) {
                                Devices.currentDevice = component.item
                                ConsoleOutputV2.update()
                            }
                        }
                    }
                    label("日志等级")
                    comboBox(arrayListOf("V", "D", "I", "W", "E"), comboBoxRenderer).apply {
                        ConsoleOutputV2.setLevel(component.item)
                        component.addActionListener {
                            if (ConsoleOutputV2.level0 != component.item && component.item != null) {
                                ConsoleOutputV2.setLevel(component.item)
                                ConsoleOutputV2.update()
                            }
                        }
                    }

                    button("清空日志") {
                        ConsoleOutputV2.clear()
                    }

                    button("回到底部") {
                        ConsoleOutputV2.toEnd()
                    }
                    button("开启服务器") {
                        runServer(project)
                    }
                    button("关闭服务器") {
                       stopServer(project)
                    }
                    button("选择设备列表") {
                        selectDevice()
                    }

                }
            }
        }
        topPanel.add(top)

        val consoleView = ConsoleViewImpl(project, true)
        ConsoleOutputV2.console = consoleView
        ConsoleOutputV2.isInitOutput = false
        consoleView.print("欢迎使用 Autojsx!", ConsoleViewContentType.LOG_DEBUG_OUTPUT)
        consoleView.performWhenNoDeferredOutput {
            ConsoleOutputV2.console!!.flushDeferredText()
        }
        // 创建下面的JPanel并使用Box.Filler填充剩余空间
        contentPanel.add(consoleView)

        // 使用Box.Filler填充剩余空间
        contentPanel.add(Box.createVerticalGlue())
        return contentPanel
    }

    private fun consoleByTextArea(comboBoxRenderer: ListCellRenderer<Any?>?) = panel {
        indent {
            row {
                label("输出的设备")
                comboBox(Devices, comboBoxRenderer).apply {
                    Devices.component = component
                    Devices.currentDevice = component.item
                    component.addActionListener {
                        Devices.currentDevice = component.item
                        println(component.item)
                    }
                }

                button("清空日志") {
                    ConsoleOutput_V1.clear()
                }
            }
        }

        row {
            //多行文本
            textArea().component.apply {
                columns = 1024
                rows = ConsoleOutput_V1.rows
                lineWrap = true //激活自动换行功能
                wrapStyleWord = true// 激活断行不断字功能
                isEditable = false
                ConsoleOutput_V1.console = this
                ConsoleOutput_V1.updateConsole()
            }
        }
    }


    override fun getIcon(): Icon {
        return ICONS.LOGO_16
    }
}
