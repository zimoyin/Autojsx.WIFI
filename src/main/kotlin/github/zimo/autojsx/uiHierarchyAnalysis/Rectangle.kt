package github.zimo.autojsx.uiHierarchyAnalysis

/**
 *
 * @author : zimo
 * @date : 2024/08/04
 */
data class Rectangle(val left: Int, val top: Int, val right: Int, val bottom: Int, val width: Int, val height: Int) {
    constructor(left: Int, top: Int, right: Int, bottom: Int) : this(
        left,
        top,
        right,
        bottom,
        right - left,
        bottom - top
    )

    val leftTop = Point(left, top)
    val rightTop = Point(right, top)
    val leftBottom = Point(left, bottom)
    val rightBottom = Point(right, bottom)

    /**
     * 判断入参矩形是否在当前矩形下
     */
    fun isRectangleInThisRectangle(rect: Rectangle): Boolean {
        return isPointInRectangle(rect.leftTop) &&
                isPointInRectangle(rect.rightTop) &&
                isPointInRectangle(rect.leftBottom) &&
                isPointInRectangle(rect.rightBottom)
    }

    /**
     * 判断点是否在矩阵中
     */
    fun isPointInRectangle(point: Point): Boolean {
        val (x,y) = point
        val d1 = crossProduct(Point(x - leftTop.x, y - leftTop.y), Point(rightTop.x - leftTop.x, rightTop.y - leftTop.y))
        val d2 = crossProduct(Point(x - rightTop.x, y - rightTop.y), Point(rightBottom.x - rightTop.x, rightBottom.y - rightTop.y))
        val d3 = crossProduct(Point(x - rightBottom.x, y - rightBottom.y), Point(leftBottom.x - rightBottom.x, leftBottom.y - rightBottom.y))
        val d4 = crossProduct(Point(x - leftBottom.x, y - leftBottom.y), Point(leftTop.x - leftBottom.x, leftTop.y - leftBottom.y))

        return (d1 >= 0 && d2 >= 0 && d3 >= 0 && d4 >= 0) || (d1 <= 0 && d2 <= 0 && d3 <= 0 && d4 <= 0)
    }




    private fun crossProduct(p1: Point, p2: Point): Int {
        return p1.x * p2.y - p1.y * p2.x
    }
}