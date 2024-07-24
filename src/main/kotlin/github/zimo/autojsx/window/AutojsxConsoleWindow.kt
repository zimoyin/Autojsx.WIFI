package github.zimo.autojsx.window

import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.panel
import github.zimo.autojsx.icons.ICONS
import github.zimo.autojsx.server.ConsoleOutputV2
import github.zimo.autojsx.server.ConsoleOutput_V1
import github.zimo.autojsx.server.Devices
import github.zimo.autojsx.server.VertxCommandServer
import github.zimo.autojsx.util.*
import io.vertx.core.json.JsonObject
import java.awt.Dimension
import java.awt.FlowLayout
import java.io.File
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.ListCellRenderer


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
//                            runProject(file, project)
                            zipProject(file, project) {
                                VertxCommandServer.Command.runProject(it.zipPath)
                                logI("项目正在上传: " + it.projectJsonPath)
                                logI("正在上传 src: " + it.srcPath)
                                logI("项目正在上传 resources: " + it.resourcesPath)
                                logI("项目正在上传 lib: " + it.libPath + "\r\n")
                            }
                        }
                    }.apply {
                        component.icon = ICONS.START_16
                    }
                    button("停止所有项目") {
                        VertxCommandServer.Command.stopAll()
                        ConsoleOutputV2.systemPrint("Action/I: 停止所有脚本指令已发送")
                    }.apply {
                        component.icon = ICONS.STOP_16
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

    @Deprecated("TODO")
    private fun runProject(file: VirtualFile, project: Project) {
        val projectJson = File(file.path)
        val json = JsonObject(projectJson.readText())

        val name = json.getString("name")
        val src = projectJson.resolve(json.getString("srcPath")).canonicalFile
        val resources = projectJson.resolve(json.getString("resources")).canonicalFile
        val lib = projectJson.resolve(json.getString("lib")).canonicalFile

        val zip = File(project?.basePath + "/build-output" + "/${name}.zip")
        zip.parentFile.mkdirs()
        zip.delete()

        executor.submit {
            zip(
                arrayListOf(src.path, resources.path, lib.path),
                project?.basePath + File.separator + "build-output" + File.separator + "${name}.zip"
            )
    //                ConsoleOutputV2.systemPrint("文件打包完成: "+project?.basePath+"/build-output"+"/${name}.zip")
            VertxCommandServer.Command.runProject(zip.canonicalPath)
        }
    }

    @Deprecated("废弃")
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
}
