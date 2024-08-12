package github.zimo.autojsx.window.hierarchy

import github.zimo.autojsx.uiHierarchyAnalysis.UINode
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

/**
 *
 * @author : zimo
 * @date : 2024/08/10
 */
class HierarchyTableState {
    private val columnNames = arrayOf("Property", "Value")
    private val data = arrayOf(
        arrayOf("Property", "Value"),
    )
    private val tableModel = object : DefaultTableModel(data, columnNames) {
        override fun isCellEditable(row: Int, column: Int): Boolean {
            return false  // 禁止所有单元格编辑
        }
    }
    val table = JTable(tableModel)

    fun clear(){
        tableModel.rowCount = 0
        table.revalidate()
        table.repaint()
    }

    fun add(v:Any,v2:Any){
        add(arrayOf(v.toString(),v2.toString()))
    }

    fun add(row: Array<String>) {
        tableModel.addRow(row)
        table.revalidate()
        table.repaint()
    }

    fun update(array: Array<Array<String>>){
        clear()
        array.forEach { row ->
            tableModel.addRow(row)
        }
        table.revalidate()
        table.repaint()
    }

    fun update(node:UINode){
        clear()
        val newData = arrayOf(
            arrayOf("index", node.index),
            arrayOf("depth", node.depth),
            arrayOf("text", node.text),
            arrayOf("id", node.id),
            arrayOf("className", node.className),
            arrayOf("packageName", node.packageName),
            arrayOf("applicationPackageName", node.applicationPackageName),
            arrayOf("desc", node.desc),
            arrayOf("bounds", node.bounds.str),
            arrayOf("width", node.bounds.width),
            arrayOf("height", node.bounds.height),
            arrayOf("checkable", node.checkable),
            arrayOf("checked", node.checked),
            arrayOf("clickable", node.clickable),
            arrayOf("enabled", node.enabled),
            arrayOf("focusable", node.focusable),
            arrayOf("focused", node.focused),
            arrayOf("scrollable", node.scrollable),
            arrayOf("longClickable", node.longClickable),
            arrayOf("password", node.password),
            arrayOf("selected", node.selected),
            arrayOf("childNodes", node.childNodes.size),
            arrayOf("", ""),
            arrayOf("", ""),
            arrayOf("", ""),
            arrayOf("", ""),
            arrayOf("", ""),
            arrayOf("", ""),
        )

        newData.forEach { row ->
            tableModel.addRow(row)
        }
        table.revalidate()
        table.repaint()
    }
}