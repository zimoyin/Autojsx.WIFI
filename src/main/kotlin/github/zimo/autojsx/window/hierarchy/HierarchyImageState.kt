package github.zimo.autojsx.window.hierarchy

import github.zimo.autojsx.uiHierarchyAnalysis.Point
import github.zimo.autojsx.uiHierarchyAnalysis.Rectangle
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel


data class HierarchyImageState(
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
    var label: JLabel = JLabel()
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

    fun update(
        scaledIcon: ImageIcon,
        hierarchy: HierarchyState? = null,
        tree: HierarchyTreeState? = null,
        table: HierarchyTableState? = null
    ) {
        val newWidth = panel.width
        val newHeight = panel.height
        label = JLabel(scaledIcon)
        panel.removeAll()
        panel.add(label)
        panel.revalidate()
        panel.repaint()
        label.addMouseListener(object : MouseAdapter() {
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
                    hierarchy?.let {
                        hierarchy.selectImagePoint = Point(originalClickX, originalClickY)
                        hierarchy.selectOriginalImagePoint = Point(originalClickX, originalClickY)
                        hierarchy.updateSelectNode()
                        // 选中点击位置所在打的节点后，更新树和表格。树更新模式为：选中树中的节点
                        hierarchy.selectNode?.let {
                            tree?.selectNodeInTree(it)
                            table?.update(it)
                        }
                        update(resizeAndRedrawRectangleImage(hierarchy).getImageIcon(hierarchy), hierarchy, tree, table)
                    }

                } else {
//                    logI("Clicked outside the image bounds")
                    hierarchy?.let {
                        hierarchy.selectImagePoint = null
                        hierarchy.selectOriginalImagePoint = null
                        hierarchy.updateSelectNode()
                        update(resizeAndRedrawRectangleImage(hierarchy).getImageIcon(hierarchy), hierarchy, tree, table)
                    }
                }
            }
        })
    }

    fun BufferedImage?.getImageIcon(state: HierarchyState): ImageIcon {
        if (this == null) return ImageIcon()
        return ImageIcon(resizeAndRedrawRectangleImage(state))
    }

    fun getImageIcon(state: HierarchyState? = null): ImageIcon {
        if (image == null) {
            return ImageIcon()
        }
        return ImageIcon(resizeAndRedrawRectangleImage(state))
    }

    /**
     * 缩放并重绘图片
     */
    fun resizeAndRedrawRectangleImage(state: HierarchyState? = null): BufferedImage? {
        return image?.getScaledInstance(
            imageWidth, imageHeight, Image.SCALE_AREA_AVERAGING
        ).let {
            state?.let { it1 -> redrawRectangleImage(it, it1) } ?: it?.run {
                val bimage = BufferedImage(
                    getWidth(null), getHeight(null), BufferedImage.TYPE_INT_ARGB
                )
                val bGr = bimage.createGraphics()
                bGr.drawImage(this, 0, 0, null)
                bGr.dispose()
                bimage
            }
        }
    }

    /**
     * 重绘图片
     */
    fun redrawRectangleImage(image: Image?, state: HierarchyState): BufferedImage? {
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

    /**
     * 计算图片在控件上的位置偏移量
     * @param newWidth 控件宽度
     */
    fun offsetImageSize(newWidth: Int, newHeight: Int): Pair<Int, Int> {
        val imageXOffset = (newWidth - imageWidth) / 2
        val imageYOffset = (newHeight - imageHeight) / 2
        return Pair(imageXOffset, imageYOffset)
    }

    /**
     * 将控件上的点位映射到控件上的图片的点位
     * @param x 控件上 的坐标
     * @param newWidth 控件的宽度
     */
    fun mapImagePoint(x: Int, y: Int,newWidth: Int, newHeight: Int): Point {
        val imageXOffset = (newWidth - imageWidth) / 2
        val imageYOffset = (newHeight - imageHeight) / 2
        val clickX = x - imageXOffset
        val clickY = y - imageYOffset
        return Point(clickX, clickY)
    }

    /**
     * 映射到原始图片
     * @param x 以当前图片左上角为原点
     * @param y 以当前图片左上角为原点
     */
    fun mapOriginalImagePoint(x: Int, y: Int): Point {
        val scaleX = originalImageWidth.toDouble() / imageWidth.toDouble()
        val scaleY = originalImageHeight.toDouble() / imageHeight.toDouble()
        val originalClickX = (x * scaleX).toInt()
        val originalClickY = (y * scaleY).toInt()
        return Point(originalClickX, originalClickY)
    }

    fun offsetComponentInfo(width: Int, height: Int): OffsetInfo {
        val scaleX = originalImageWidth.toDouble() / imageWidth.toDouble()
        val scaleY = originalImageHeight.toDouble() / imageHeight.toDouble()
        val imageXOffset = (width - imageWidth) / 2
        val imageYOffset = (height - imageHeight) / 2
        return OffsetInfo(width,height,imageXOffset,imageYOffset,scaleX,scaleY)
    }

    data class OffsetInfo(
        val componentWith: Int,
        val componentHeight: Int,
        /**
         * 图片到控件 X 的距离
         */
        val offsetComponentToImageOriginX: Int,
        val offsetComponentToImageOriginY: Int,
        /**
         * 图片和原图的比例
         */
        val scaleX: Double,
        val scaleY: Double
    ){
        /**
         * 将控件上的点位映射到控件上的图片的点位
         * @param x 控件上 的坐标
         * @param y 控件上 的坐标
         */
        fun offsetComponentPoint(x: Int, y: Int): Point {
            return Point(x - offsetComponentToImageOriginX, y - offsetComponentToImageOriginY)
        }

        /**
         * 将控件上的点位映射到原图的点位
         */
        fun offsetComponentToOriginImagePoint(x: Int, y: Int): Point {
            val point = offsetComponentPoint(x, y)
            val originalClickX = (point.x * scaleX).toInt()
            val originalClickY = (point.y * scaleY).toInt()
            return Point(originalClickX, originalClickY)
        }

        /**
         * 将图片的点位映射到原图的点位
         */
        fun offsetImageToOriginImagePoint(x: Int, y: Int): Point {
            val originalClickX = (x * scaleX).toInt()
            val originalClickY = (y * scaleY).toInt()
            return Point(originalClickX, originalClickY)
        }
    }
}