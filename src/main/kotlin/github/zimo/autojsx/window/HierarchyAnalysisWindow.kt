package github.zimo.autojsx.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import github.zimo.autojsx.uiHierarchyAnalysis.Point
import github.zimo.autojsx.uiHierarchyAnalysis.UIHierarchy
import github.zimo.autojsx.uiHierarchyAnalysis.UINode
import github.zimo.autojsx.util.logI
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
//@Deprecated("因关键技术缺失，废弃该方法")
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
        mainJPanel.layout = BorderLayout()

        // Load the image
        val image = loadLastImage(p0)
        val originalIcon = ImageIcon(image?.path)

        // Calculate the aspect ratio of the original image
        val aspectRatio = originalIcon.iconWidth.toDouble() / originalIcon.iconHeight.toDouble()

        // Add a component listener to the JPanel to detect size changes
        mainJPanel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                // Get the new dimensions of the JPanel
                val newWidth = mainJPanel.width
                val newHeight = mainJPanel.height

                // Determine the new dimensions to maintain the aspect ratio
                val scaledWidth: Int
                val scaledHeight: Int

                // Determine which dimension to scale
                if (newWidth / aspectRatio <= newHeight) {
                    scaledWidth = newWidth
                    scaledHeight = (newWidth / aspectRatio).toInt()
                } else {
                    scaledWidth = (newHeight * aspectRatio).toInt()
                    scaledHeight = newHeight
                }

                // Scale the image to fit the JPanel
                val scaledImage =
                    originalIcon.image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_AREA_AVERAGING)
                // 重绘图片
                val scaledIcon = ImageIcon(scaledImage)

                // Update the JLabel with the scaled icon
                val jLabel = JLabel(scaledIcon)
                mainJPanel.removeAll()
                mainJPanel.add(jLabel)
                mainJPanel.revalidate()
                mainJPanel.repaint()
                jLabel.addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        // Calculate the image's position inside the JPanel
                        val imageXOffset = (newWidth - scaledWidth) / 2
                        val imageYOffset = (newHeight - scaledHeight) / 2

                        // Calculate the click position relative to the image
                        val clickX = e.x - imageXOffset
                        val clickY = e.y - imageYOffset

                        // Calculate the scale ratios
                        val scaleX = originalIcon.iconWidth.toDouble() / scaledWidth.toDouble()
                        val scaleY = originalIcon.iconHeight.toDouble() / scaledHeight.toDouble()

                        // Map the click position to the original image size
                        val originalClickX = (clickX * scaleX).toInt()
                        val originalClickY = (clickY * scaleY).toInt()

                        if (clickX in 0 until scaledWidth + 1 && clickY in 0 until scaledHeight + 1) {
                            logI("Clicked at ($clickX, $clickY) relative to the image")
                            logI("Clicked at ($originalClickX, $originalClickY) relative to the original image")
                        } else {
                            logI("Clicked outside the image bounds")
                        }
                    }
                })
            }
        })

        mainJPanel.add(JLabel(originalIcon), BorderLayout.CENTER)
        mainJPanel.minimumSize = Dimension(100, 100)
        mainJPanel.background = Color.red
        return mainJPanel
    }


    fun onUpdateImage(callback: () -> Unit) {

    }

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

data class ImageState(
    val jPanel: JPanel,
    val imageWidth:Int,
    val imageHeight:Int,
    val originalImageWidth:Int,
    val originalImageHeight:Int
){
    fun update(scaledIcon: ImageIcon){
        val newWidth = jPanel.width
        val newHeight = jPanel.height
        val jLabel = JLabel(scaledIcon)
        jPanel.removeAll()
        jPanel.add(jLabel)
        jPanel.revalidate()
        jPanel.repaint()
        jLabel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                // Calculate the image's position inside the JPanel
                val imageXOffset = (newWidth - imageWidth) / 2
                val imageYOffset = (newHeight - imageHeight) / 2

                // Calculate the click position relative to the image
                val clickX = e.x - imageXOffset
                val clickY = e.y - imageYOffset

                // Calculate the scale ratios
                val scaleX = originalImageWidth.toDouble() / imageWidth.toDouble()
                val scaleY = originalImageHeight.toDouble() / imageHeight.toDouble()

                // Map the click position to the original image size
                val originalClickX = (clickX * scaleX).toInt()
                val originalClickY = (clickY * scaleY).toInt()

                if (clickX in 0 until imageWidth + 1 && clickY in 0 until imageHeight + 1) {
                    logI("Clicked at ($clickX, $clickY) relative to the image")
                    logI("Clicked at ($originalClickX, $originalClickY) relative to the original image")
                } else {
                    logI("Clicked outside the image bounds")
                }
            }
        })
    }
}

data class HierarchyState(
    val hierarchy: UIHierarchy,
    val image: ImageIO,
    val selectImagePoint: Point,
    val selectOriginalImagePoint: Point,
    val selectNode: UINode
)