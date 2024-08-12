package github.zimo.autojsx.window

import github.zimo.autojsx.uiHierarchyAnalysis.Point
import java.awt.Color
import java.awt.MouseInfo
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.JWindow
import kotlin.math.max
import kotlin.math.min

class PixelMagnifier : JWindow() {
    var image: BufferedImage? = null
    var open: Boolean = true
    private val magnification = 15
    private val gridSize = 9
    private val stringLine = 43

    init {
        setSize(gridSize * magnification, gridSize * magnification + stringLine)
    }

    /**
     * @param x 鼠标在图片位置，并且是原图位置
     * @param y
     */
    fun updateMagnifier(x: Int, y: Int, originalImagePoint: Point? = null) {
        if (!open) return
        if (image == null){
            isVisible = false
            return
        }
        // 提取鼠标周围的图像区域
        val startX = max(0.0, (x - gridSize / 2).toDouble()).toInt()
        val startY = max(0.0, (y - gridSize / 2).toDouble()).toInt()
        val gridSize0 = gridSize.let {
            if (image == null) return@let it
            if (startX + gridSize > image!!.width || startY + gridSize > image!!.height) min(
                image!!.width - startX,
                image!!.height - startY
            ) else it
        }
        val subImage = image?.getSubimage(startX, startY, gridSize0, gridSize0)


        // 在窗口上绘制放大图像
        val g = graphics
        g.clearRect(0, 0, width, height)
        g.drawImage(subImage, 0, 0, gridSize * magnification, gridSize * magnification, null)


        // 绘制网格
        g.color = Color.BLACK
        for (i in 0..gridSize) {
            val pos = i * magnification
            g.drawLine(pos, 0, pos, height - stringLine)
            g.drawLine(0, pos, width, pos)
        }


        // 框选当前像素
        g.color = Color.RED
        val highlightX = (x - startX) * magnification
        val highlightY = (y - startY) * magnification
        g.drawRect(highlightX, highlightY, magnification, magnification)

        g.color = Color.BLACK
        val pixelColor = subImage?.getRGB(x - startX, y - startY) ?: 0
        val color = Color(pixelColor)
        val colorText = "color(${color.red}, ${color.green}, ${color.blue})"
        val hexColorText = "#${Integer.toHexString(pixelColor).substring(2).uppercase(Locale.getDefault())}"

        g.drawString(originalImagePoint.toString(), 3, gridSize * magnification + 13 * 3)
        g.drawString(colorText, 3, gridSize * magnification + 13)
        g.drawString(hexColorText, 3, gridSize * magnification + 13 * 2)
        // 更新窗口位置
        setLocation(
            MouseInfo.getPointerInfo().location.x + 7,
            MouseInfo.getPointerInfo().location.y + 13
        )
    }
}
