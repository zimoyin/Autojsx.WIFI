package github.zimo.autojsx.window

import ScreenshotMagnifier
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.panel
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.util.*
import github.zimo.autojsx.window.hierarchy.HierarchyImageState
import github.zimo.autojsx.window.hierarchy.HierarchyTableState
import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO
import javax.swing.*

/**
 *
 * @author : zimo
 * @date : 2024/08/11
 */
class ImageColorAnalysisWindow : ToolWindowFactory {
    val ImageState = HierarchyImageState()
    val magnifier = PixelMagnifier()

    var subImage: BufferedImage? = null
    val subImagePanelSize = Dimension(100, 100)

    val HierarchyTableState: HierarchyTableState = HierarchyTableState()

    override fun createToolWindowContent(p0: Project, window: ToolWindow) {
        val content2 = ContentFactory.getInstance().createContent(ui(p0, window), "图色分析", false)
        window.contentManager.addContent(content2)
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

    val selectionBox = ScreenshotMagnifier()
    fun imageUI(p0: Project): JPanel {
        val mainJPanel = ImageState.panel
//        ImageState.updateImage(ImageIO.read(File("C:\\Users\\zimoa\\Pictures\\98280296_p0.jpg")))
//        magnifier.image = ImageState.image

        mainJPanel.addComponentListener(componentAdapter())
        if (ImageState.image == null) {
            mainJPanel.add(JLabel("Not Found Image"), BorderLayout.CENTER)
        }else{
            mainJPanel.add(JLabel(ImageState.getImageIcon()), BorderLayout.CENTER)
        }
        return mainJPanel
    }

    private fun componentAdapter() = object : ComponentAdapter() {
        override fun componentResized(e: ComponentEvent?) {
            initImageListener(ImageState)
        }
    }

    private fun initImageListener(ImageState: HierarchyImageState) {
        if (ImageState.image == null) return
        ImageState.calculateNewDimensionsToFitPanel()
        val scaledImage = ImageState.resizeAndRedrawRectangleImage()
        val scaledIcon = if (scaledImage == null) ImageIcon() else ImageIcon(scaledImage)
        ImageState.update(scaledIcon)
        var startScreenPoint: Point? = null
        var startComponentPoint: Point? = null
        ImageState.label.addMouseListener(object : MouseAdapter() {
            override fun mouseExited(e: MouseEvent?) {
                magnifier.isVisible = false
            }

            override fun mousePressed(e: MouseEvent) {
                val x = MouseInfo.getPointerInfo().location.x
                val y = MouseInfo.getPointerInfo().location.y
                val info = ImageState.offsetComponentInfo(e.component.width, e.component.height)

                startScreenPoint = Point(x, y)
                startComponentPoint = e.point
                if (ImageState.image == null) return
                val imagePoint = info.offsetComponentToOriginImagePoint(e.x, e.y)
                val hexColorText = "#${
                    Integer.toHexString(ImageState.image?.getRGB(imagePoint.x, imagePoint.y) ?: 0).substring(2)
                        .uppercase(Locale.getDefault())
                }"
                HierarchyTableState.add("x:${imagePoint.x},y:${imagePoint.y}", hexColorText)
            }

            override fun mouseReleased(e: MouseEvent) {
                if (ImageState == subImageState) return
                val info = ImageState.offsetComponentInfo(e.component.width, e.component.height)
                val selectedRectangle = selectionBox.finalizeSelection() ?: return
                // 计算原图位置
                val leftTop = info.offsetComponentToOriginImagePoint(selectedRectangle.x, selectedRectangle.y)
                val rightBottom = info.offsetComponentToOriginImagePoint(
                    selectedRectangle.x + selectedRectangle.width,
                    selectedRectangle.y + selectedRectangle.height
                )
                val x = leftTop.x.let {
                    if (it <= 0) 0 else it
                }
                val y = leftTop.y.let {
                    if (it <= 0) 0 else it
                }
                val width = (rightBottom.x - x).let {
                    if (x + it > (ImageState.image?.width
                            ?: it)
                    ) ImageState.image!!.width - x else it
                }
                val height = (rightBottom.y - y).let {
                    if (y + it > (ImageState.image?.height
                            ?: it)
                    ) ImageState.image!!.height - y else it
                }

                kotlin.runCatching {
                    subImage = ImageState.image?.getSubimage(
                        x,
                        y,
                        width,
                        height
                    )


                    subImageInfo.removeAll()
                    subImagePanel.size = subImagePanelSize
                    subImageState.apply {
                        subImage?.let {
                            updateImage(it)
                            this.update(ImageIcon(resizeAndRedrawRectangleImage()))
                            subImageInfo.add(
                                JTextArea("Size: [x: ${x}, y: ${y}][w: ${width},h: ${height}]")
                            )
                            subImageInfo.revalidate()
                            subImageInfo.repaint()
                        }
                    }
                    initImageListener(subImageState)
                }.onFailure {
                    logW(it.message)
                }
            }
        })
        ImageState.label.addMouseMotionListener(object : MouseMotionListener {
            override fun mouseDragged(e: MouseEvent) {
                val x = MouseInfo.getPointerInfo().location.x
                val y = MouseInfo.getPointerInfo().location.y
                val screenPoint = Point(x, y)

                if (ImageState == subImageState) return
                startScreenPoint?.let {
                    selectionBox.updateSelectionRectangle(
                        it,
                        screenPoint,
                        startComponentPoint
                    )
                }
                updateMagnifier(e)
            }

            override fun mouseMoved(e: MouseEvent) {
                updateMagnifier(e)
            }

            fun updateMagnifier(e: MouseEvent) {
                if (!magnifier.open) {
                    magnifier.isVisible = false
                    return
                }
                magnifier.isVisible = true
                val newWidth = e.component.width
                val newHeight = e.component.height
                val info = ImageState.offsetComponentInfo(newWidth, newHeight)
                val imageWidth = ImageState.imageWidth
                val imageHeight = ImageState.imageHeight

                // 判断鼠标是否超出了图片范围
                val imageXOffset = info.offsetComponentToImageOriginX
                val imageYOffset = info.offsetComponentToImageOriginY
                if (e.x !in imageXOffset until imageXOffset + imageWidth ||
                    e.y !in imageYOffset until imageYOffset + imageHeight
                ) {
                    magnifier.isVisible = false
                    return
                }

                // Map the click position to the original image size
                val originalImagePoint = info.offsetComponentToOriginImagePoint(e.x, e.y)
                val originalClickX = originalImagePoint.x
                val originalClickY = originalImagePoint.y
                magnifier.updateMagnifier(originalClickX, originalClickY, originalImagePoint)
            }
        })
    }

    val subImageInfo: JPanel = JPanel()
    val subImagePanel: JPanel = object : JPanel() {
        override fun getPreferredSize(): Dimension {
            return subImagePanelSize
        }

    }.apply {
        this.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLUE),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        )
        this.background = Color.GRAY
    }

    val subImageState = HierarchyImageState(subImagePanel)
    fun buttonUI(p0: Project): JPanel {
        val mainJPanel = JPanel()
        val tableScrollPane = JScrollPane(HierarchyTableState.table)
        mainJPanel.add(panel {
            row {
                button("截图") {
                    executor.execute {
                        VertxCommand.getScreenshot {
                            if (it.isEmpty()) {
                                logE("获取截图失败")
                                return@getScreenshot
                            }
                            val image = base64_image(it)
                            if (image != null) {
                                ImageState.updateImage(image)
                                ImageState.update(ImageState.getImageIcon())
                                magnifier.image = ImageState.image
                                ImageState.panel.revalidate()
                                ImageState.panel.repaint()
                                initImageListener(ImageState)
                            } else {
                                logE("获取截图失败")
                            }
                        }
                    }
                }
                button("拾色板") {
                    magnifier.open = !magnifier.open
                }
                button("清空颜色") {
                    HierarchyTableState.clear()
                }
            }

            row {
                cell(tableScrollPane)
            }

            // end
            row {
                cell(subImagePanel)
                button("保存图片") {
                    if (subImage != null) {
                        val fileChooser = JFileChooser()
                        fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                        fileChooser.dialogTitle = "选择保存路径"
                        val result = fileChooser.showSaveDialog(subImageInfo)
                        if (result == JFileChooser.APPROVE_OPTION) {
                            val selectedFile = fileChooser.selectedFile
                            val fileName = "subImage_${System.currentTimeMillis()}.png"
                            val file = File(selectedFile, fileName)
                            try {
                                ImageIO.write(subImage, "png", file)
                                logI("Image saved to ${file.absolutePath}")
                            } catch (e: IOException) {
                                logE("Error saving image: ${e.message}")
                            }
                        }
                    }
                }
            }
            row {
                cell(subImageInfo)
            }
        })
        return mainJPanel
    }
}