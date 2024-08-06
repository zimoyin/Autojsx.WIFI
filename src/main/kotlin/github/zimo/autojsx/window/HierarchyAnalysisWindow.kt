package github.zimo.autojsx.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Image
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.math.abs


/**
 *
 * @author : zimo
 * @date : 2024/08/04
 */
@Deprecated("因关键技术缺失，废弃该方法")
class HierarchyAnalysisWindow : ToolWindowFactory {

    override fun createToolWindowContent(p0: Project, window: ToolWindow) {
        val content = ContentFactory.getInstance().createContent(ui(p0, window), "HierarchyAnalysis", false)
        window.contentManager.addContent(content)
    }


    fun ui(p0: Project, window: ToolWindow): JPanel {
        val panel = JPanel()

        // 创建分隔面板
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imageUI(p0), buttonUI(p0))
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
        val mainJPanel = JPanel()
//        mainJPanel.layout = BorderLayout()

        // Load the image
        val image = loadLastImage(p0)
        val originalIcon = ImageIcon(image?.path)

        var panePoorWidth = 1.0
        var panePoorHeight = 1.0
        // Add a component listener to the JPanel to detect size changes
        mainJPanel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                // Get the new dimensions of the JPanel
                val newWidth = mainJPanel.width
                val newHeight = mainJPanel.height

                println("New dimensions: ($newWidth, $newHeight)")
                // Get original dimensions of the image
                val originalWidth = originalIcon.iconWidth
                val originalHeight = originalIcon.iconHeight

                println("Original dimensions: ($originalWidth, $originalHeight)")
                if (newWidth > 0 && newHeight > 0 && panePoorWidth == 1.0 && panePoorHeight == 1.0) {
                    panePoorWidth = abs(newWidth.toDouble()/originalWidth )
                    panePoorHeight = abs(newHeight.toDouble()/originalHeight)
                }
                println("Pane poor: ($panePoorWidth, $panePoorHeight)")

                val scaledWidth: Int = (newWidth * panePoorWidth).toInt()
                val scaledHeight: Int = (newHeight * panePoorHeight).toInt()

                println("Scaled dimensions: ($scaledWidth, $scaledHeight)")
                // Scale the image to fit the JPanel
                val scaledImage = originalIcon.image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH)
                val scaledIcon = ImageIcon(scaledImage)

                // Update the JLabel with the scaled icon
                val jLabel = JLabel(scaledIcon)
                mainJPanel.removeAll()
                mainJPanel.add(jLabel)
                mainJPanel.revalidate()
                mainJPanel.repaint()
                jLabel.addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        val clickX = e.x
                        val clickY = e.y

                        // Convert the click coordinates to the original image coordinates
                        val originalX = (clickX * panePoorWidth).toInt()
                        val originalY = (clickY * panePoorHeight).toInt()

                        println("Click coordinates: ($clickX, $clickY)")
                        println("Original image coordinates: ($originalX, $originalY)")
                    }
                })
            }
        })

        mainJPanel.add(JLabel(originalIcon))
        mainJPanel.minimumSize = Dimension(100, 100)
        mainJPanel.background = Color.red
        return mainJPanel
    }

    // 扩展函数来计算两个整数的最大公约数
    fun Int.gcd(other: Int): Int = if (other == 0) this else other.gcd(this % other)

    fun buttonUI(p0: Project): JPanel {
        val mainJPanel = JPanel()
        mainJPanel.add(JButton("分析层次"))
        mainJPanel.minimumSize = Dimension(100, 100)
        return mainJPanel
    }

    fun loadLastImage(e0: Project): File? {
        val path = e0.basePath + File.separator + "/build/autojs/images/"
        val file = File(path).listFiles()?.filter { it.extension == "png" }?.maxBy { it.nameWithoutExtension.toLong() }
        return file
    }
}