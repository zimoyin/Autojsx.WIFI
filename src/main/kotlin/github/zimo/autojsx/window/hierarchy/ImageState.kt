package github.zimo.autojsx.window.hierarchy

import github.zimo.autojsx.uiHierarchyAnalysis.Point
import github.zimo.autojsx.uiHierarchyAnalysis.Rectangle
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel

data class ImageState(
    val panel: JPanel = JPanel().apply {
        layout = BorderLayout()
        minimumSize = Dimension(100, 100)
    },
) {
    var imageWidth: Int = -1
        private set
    var imageHeight: Int = -1
        private set
    var originalImageWidth: Int = -1
        private set
    var originalImageHeight: Int = -1
        private set
    var aspectRatio: Double = -1.0
        private set
    var image: BufferedImage? = null
        private set

    fun updateImage(img: BufferedImage) {
        image = img
        imageWidth = img.width
        imageHeight = img.height
        originalImageWidth = img.width
        originalImageHeight = img.height
        aspectRatio = img.width.toDouble() / img.height.toDouble()
        calculateNewDimensionsToFitPanel()
    }


    /**
     * 计算新尺寸适配窗体
     */
    fun calculateNewDimensionsToFitPanel() {
        aspectRatio = originalImageWidth.toDouble() / originalImageHeight.toDouble()
        val newWidth = panel.width
        val newHeight = panel.height
        if (newWidth / aspectRatio <= newHeight) {
            imageWidth = newWidth
            imageHeight = (newWidth / aspectRatio).toInt()
        } else {
            imageWidth = (newHeight * aspectRatio).toInt()
            imageHeight = newHeight
        }
    }

    fun update(scaledIcon: ImageIcon, state: HierarchyState, tree: TreeState, table: TableState) {
        val newWidth = panel.width
        val newHeight = panel.height
        val jLabel = JLabel(scaledIcon)
        panel.removeAll()
        panel.add(jLabel)
        panel.revalidate()
        panel.repaint()
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
//                    logI("Clicked at ($clickX, $clickY) relative to the image")
//                    logI("Clicked at ($originalClickX, $originalClickY) relative to the original image")
                    state.selectImagePoint = Point(originalClickX, originalClickY)
                    state.selectOriginalImagePoint = Point(originalClickX, originalClickY)
                    state.updateSelectNode()
                    state.selectNode?.let {
                        tree.selectNodeInTree(it)
                        table.update(it)
                    }
                    update(redrawImage(state).getImageIcon(state), state, tree, table)
                } else {
//                    logI("Clicked outside the image bounds")
                    state.selectImagePoint = null
                    state.selectOriginalImagePoint = null
                    state.updateSelectNode()
                    update(redrawImage(state).getImageIcon(state), state, tree, table)
                }
            }
        })
    }

    fun BufferedImage?.getImageIcon(state: HierarchyState): ImageIcon {
        if (this == null) return ImageIcon()
        return ImageIcon(redrawImage(state))
    }

    fun getImageIcon(state: HierarchyState? = null): ImageIcon {
        if (image == null) {
            return ImageIcon()
        }
        return if (state == null) ImageIcon(image) else ImageIcon(redrawImage(state))
    }

    /**
     * 缩放并重绘图片
     */
    fun redrawImage(state: HierarchyState): BufferedImage? {
        return image?.getScaledInstance(
            imageWidth, imageHeight, Image.SCALE_AREA_AVERAGING
        ).let {
            redrawImage(it, state)
        }
    }

    /**
     * 重绘图片
     */
    fun redrawImage(image: Image?, state: HierarchyState): BufferedImage? {
        if (image == null) return null
        val rectangle = state.selectNode?.bounds?.toRectangle()?.toOriginalRectangle()
        val newImage = BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB)
        val g2d = newImage.createGraphics()
        g2d.drawImage(image, 0, 0, null)
        g2d.color = Color.RED
        g2d.stroke = BasicStroke(1.25f)
        if (rectangle != null) {
            g2d.drawRect(rectangle.left, rectangle.top, rectangle.width, rectangle.height)
            var node = state.selectNode?.parent
            g2d.color = Color.GREEN
            var offset = 1
            while (node != null && node != state.selectNode) {
                val nodeRectangle = node.bounds.toRectangle().toOriginalRectangle()
                g2d.drawRect(
                    nodeRectangle.left + offset,
                    nodeRectangle.top + offset,
                    nodeRectangle.width + offset,
                    nodeRectangle.height + offset
                )
                offset--
                node = node.parent
            }
        }
        return newImage
    }

    /**
     * 求该矩形在缩放后的图片中的位置
     */
    private fun Rectangle.toOriginalRectangle(): Rectangle {
        val it = this
        val scaleX = imageWidth.toDouble() / originalImageWidth.toDouble()
        val scaleY = imageHeight.toDouble() / originalImageHeight.toDouble()
        val left = (it.left * scaleX).toInt()
        val top = (it.top * scaleY).toInt()
        val right = (it.right * scaleX).toInt()
        val bottom = (it.bottom * scaleY).toInt()
        return Rectangle(left, top, right, bottom)
    }
}