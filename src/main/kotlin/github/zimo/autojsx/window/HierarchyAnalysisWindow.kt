package github.zimo.autojsx.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.panel
import java.awt.BorderLayout
import java.awt.Button
import java.awt.Color
import java.awt.Dimension
import java.awt.Image
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.io.File
import javax.swing.*

/**
 *
 * @author : zimo
 * @date : 2024/08/04
 */
class HierarchyAnalysisWindow : ToolWindowFactory {

    override fun createToolWindowContent(p0: Project, window: ToolWindow) {
        val content = ContentFactory.getInstance().createContent(ui(p0,window), "HierarchyAnalysis", false)
        window.contentManager.addContent(content)
    }


    fun ui(p0: Project, window: ToolWindow): JPanel {
        val size = window.component.size
        val panel = JPanel()

        // 创建分隔面板
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imageUI(p0), buttonUI(p0))
        splitPane.dividerLocation = 200 // 设置分隔线的初始位置，可以根据需要调整
        splitPane.setDividerSize(3)

        // 添加组件监听器，监听窗口布局完成事件
        window.component.addComponentListener(object : ComponentAdapter() {
            override fun componentShown(e: ComponentEvent?) {
                val windowSize: Dimension = window.component.size
                if (windowSize.width != 0 && windowSize.height != 0) splitPane.dividerLocation = size.width/2
                println("Width: ${windowSize.width}, Height: ${windowSize.height}")
            }
        })

        // 创建主面板，并将分隔面板添加到主面板中
        panel.layout = BorderLayout()
        panel.add(splitPane, BorderLayout.CENTER)

        return panel
    }

    fun imageUI(p0: Project): JPanel {
        val mainJPanel = JPanel()

        // Load the image
        val image = loadLastImage(p0)
        val originalIcon = ImageIcon(image?.path)

        // Add a component listener to the JPanel to detect size changes
//        mainJPanel.addComponentListener(object : ComponentAdapter() {
//            override fun componentResized(e: ComponentEvent?) {
//                // Get the new dimensions of the JPanel
//                val newWidth = mainJPanel.width
//                val newHeight = mainJPanel.height
//
//                // Get original dimensions of the image
//                val originalWidth = originalIcon.iconWidth
//                val originalHeight = originalIcon.iconHeight
//
//                // Calculate the scale factors while keeping the aspect ratio
//                val widthRatio = newWidth / originalWidth
//                val heightRatio = newHeight / originalHeight
//
//                // Choose the smaller ratio and ensure it's an integer
//                val scaleFactor = minOf(widthRatio, heightRatio).coerceAtLeast(1)
//
//                // Scale the image to fit the JPanel
//                val scaledWidth = originalWidth * scaleFactor
//                val scaledHeight = originalHeight * scaleFactor
//                val scaledImage = originalIcon.image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH)
//                val scaledIcon = ImageIcon(scaledImage)
//
//                // Update the JLabel with the scaled icon
//                mainJPanel.removeAll()
//                mainJPanel.add(JLabel(scaledIcon))
//                mainJPanel.revalidate()
//                mainJPanel.repaint()
//            }
//        })

        // Initialize with the original size
//        mainJPanel.add(JLabel(originalIcon))
        mainJPanel.add(Button("测试"))
        return mainJPanel
    }

    fun buttonUI(p0: Project): JPanel {
        val mainJPanel = JPanel()
        mainJPanel.add(JButton("分析层次"))
        return mainJPanel
    }

    fun loadLastImage(e0: Project): File? {
        val path =  e0.basePath + File.separator + "/build/autojs/images/"
        val file = File(path).listFiles()?.filter { it.extension == "png" }?.maxBy { it.nameWithoutExtension.toLong() }
        return file
    }
}