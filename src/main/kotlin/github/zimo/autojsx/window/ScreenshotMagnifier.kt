import java.awt.*
import javax.swing.JWindow
import kotlin.math.abs

class ScreenshotMagnifier : JWindow() {
    private var selectionRectangle: Rectangle? = null

    private var startComponentPoint: Point? = null

    init {
        background = Color(0, 0, 0, 0) // 设置背景为透明
    }

    fun updateSelectionRectangle(startPoint: Point, endPoint: Point, startComponentPoint0: Point?) {
        val x = minOf(startPoint.x, endPoint.x)
        val y = minOf(startPoint.y, endPoint.y)
        val width = abs(startPoint.x - endPoint.x)
        val height = abs(startPoint.y - endPoint.y)

        startComponentPoint = startComponentPoint0
        selectionRectangle = Rectangle(x, y, width, height)
        bounds = selectionRectangle!!
        isVisible = true
        repaint()
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        val g2d = g as Graphics2D
        g2d.color = Color.RED
        g2d.stroke = BasicStroke(2f)
        g2d.drawRect(0, 0, width - 1, height - 1)
    }

    fun finalizeSelection(): Rectangle? {
        isVisible = false
        return startComponentPoint?.let {
            if (selectionRectangle == null) return@let null
            Rectangle(it.x, it.y, selectionRectangle!!.width, selectionRectangle!!.height)
        } ?: run {
            null
        }
    }
}
