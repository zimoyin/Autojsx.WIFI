package github.zimo.autojsx.window

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.server.VertxServer
import github.zimo.autojsx.util.base64_image
import github.zimo.autojsx.util.executor
import github.zimo.autojsx.util.logE
import github.zimo.autojsx.window.hierarchy.*
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.*


/**
 *
 * @author : zimo
 * @date : 2024/08/04
 */
class HierarchyAnalysisWindow : ToolWindowFactory {
    val ImageState = HierarchyImageState()
    val HierarchyState = HierarchyState()
    val TreeState = HierarchyTreeState()
    val TableState = HierarchyTableState()

    override fun createToolWindowContent(p0: Project, window: ToolWindow) {
        val content = ContentFactory.getInstance().createContent(ui(p0, window), "布局分析", false)
        window.contentManager.addContent(content)
        ImageColorAnalysisWindow().createToolWindowContent(p0, window)
    }


    fun ui(p0: Project, window: ToolWindow): JPanel {
        val panel = JPanel()

        // 创建分隔面板
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imageUI(p0), buttonUI(p0))
//        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, JLabel("445"), buttonUI(p0))
        splitPane.dividerLocation = 200 // 设置分隔线的初始位置，可以根据需要调整
        splitPane.setDividerSize(3)
        splitPane.isEnabled = true
        var userIsDragging = false

        // 添加组件监听器，监听窗口布局完成事件
        window.component.addComponentListener(object : ComponentAdapter() {
            override fun componentShown(e: ComponentEvent?) {
                val windowSize: Dimension = window.component.size
                if (windowSize.width > 0 && windowSize.height >= 0) splitPane.dividerLocation = windowSize.width / 2
            }
        })

        // 创建主面板，并将分隔面板添加到主面板中
        panel.layout = BorderLayout()
        panel.add(splitPane, BorderLayout.CENTER)

        return panel
    }


    fun imageUI(p0: Project): JPanel {
        val mainJPanel = ImageState.panel

        mainJPanel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                if (ImageState.image != null) {
                    ImageState.calculateNewDimensionsToFitPanel()
                    val scaledImage = ImageState.resizeAndRedrawRectangleImage(HierarchyState)
                    val scaledIcon = if (scaledImage == null) ImageIcon() else ImageIcon(scaledImage)
                    ImageState.update(scaledIcon, HierarchyState, TreeState, TableState)
                }
                mainJPanel.revalidate()
                mainJPanel.repaint()
            }

            override fun componentMoved(e: ComponentEvent?) {
                super.componentMoved(e)
                mainJPanel.revalidate()
                mainJPanel.repaint()
            }

            override fun componentShown(e: ComponentEvent?) {
                super.componentShown(e)
                mainJPanel.revalidate()
                mainJPanel.repaint()
            }
        })

        if (ImageState.image == null) {
            mainJPanel.add(JLabel("Not Found Image"), BorderLayout.CENTER)
        }else{
            mainJPanel.add(JLabel(ImageState.getImageIcon()), BorderLayout.CENTER)
        }
        return mainJPanel
    }

    fun buttonUI(p0: Project): JPanel {
        val mainJPanel = JPanel()

        val treeScrollPane = JScrollPane(TreeState.tree).apply {
            TreeState.init(ImageState, HierarchyState, TableState)
        }
        val tableScrollPane = JScrollPane(TableState.table)
        mainJPanel.add {
            row {
                cell(buttonHierarchyAnalysis(p0))
                button("生成代码"){
                    HierarchyGeneratingCode(HierarchyState).generateCode()
                }
            }
            row { cell(treeScrollPane) }
            row { cell(tableScrollPane) }
        }

        mainJPanel.minimumSize = Dimension(100, 100)
        mainJPanel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                // 获取主面板的新尺寸
                val newSize = e.component.size
                if (newSize.width <= 0 || newSize.height <= 0) return
                // 设置 JScrollPane 的新尺寸
                treeScrollPane.preferredSize = Dimension(newSize.width - 2, newSize.height / 2 - 2)
                treeScrollPane.size = Dimension(newSize.width - 2, newSize.height / 2 - 2)
                tableScrollPane.preferredSize = Dimension(newSize.width - 2, newSize.height / 2 - 2)
                tableScrollPane.size = Dimension(newSize.width - 2, newSize.height / 2 - 2)
                mainJPanel.revalidate()  // 重新布局组件
                mainJPanel.repaint()     // 重绘组件
            }

            override fun componentMoved(e: ComponentEvent?) {
                super.componentMoved(e)
                mainJPanel.revalidate()  // 重新布局组件
                mainJPanel.repaint()     // 重绘组件
            }

            override fun componentShown(e: ComponentEvent?) {
                super.componentShown(e)
                mainJPanel.revalidate()  // 重新布局组件
                mainJPanel.repaint()     // 重绘组件
            }
        })
        return mainJPanel
    }

    fun buttonHierarchyAnalysis(e: Project): JButton {
        val action = {
            VertxServer.start()
            val submit = executor.submit {
                VertxCommand.getScreenshot {
                    if (it.isEmpty()) {
                        logE("获取截图失败")
                        return@getScreenshot
                    }
                    val image = base64_image(it)
                    if (image != null) {
                        ImageState.updateImage(image)
                        HierarchyState.selectImagePoint = null
                        HierarchyState.selectOriginalImagePoint = null
                        HierarchyState.selectNode = null
                        ImageState.update(
                            ImageState.getImageIcon(HierarchyState),
                            HierarchyState,
                            TreeState,
                            TableState
                        )
                    } else {
                        logE("获取截图失败")
                    }
                }
            }
            val submit2 = executor.submit {
                VertxCommand.getNodesAsJson {
                    if (it == null) {
                        logE("获取Node失败")
                        return@getNodesAsJson
                    }
                    HierarchyState.hierarchy = it
                    TreeState.updateTree(HierarchyState)
                }
            }
            submit2.get()
            submit.get()
        }
        return JButton("分析层次").apply {
            this.addActionListener {
                ProgressManager.getInstance().run(object : Task.Backgroundable(e, "分析层次...", true) {
                    override fun run(indicator: ProgressIndicator) {
                        action()
                    }
                })
            }
        }
    }
}

private fun JPanel.add(callback: Panel.() -> Unit) {
    this.add(panel { callback() })
}