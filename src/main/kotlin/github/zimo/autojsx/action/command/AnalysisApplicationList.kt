package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.table.JBTable
import github.zimo.autojsx.pojo.ApplicationListPojo
import github.zimo.autojsx.server.VertxCommandServer
import github.zimo.autojsx.util.executor
import github.zimo.autojsx.util.logW
import java.awt.Component
import java.awt.Dimension
import java.awt.Image
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel


/**
 * 1. 当前应用
 * 2. 安装的其他应用
 */
class AnalysisApplicationList :
    AnAction("分析应用列表", "获取手机安装的应用信息等", github.zimo.autojsx.icons.ICONS.LOGO_16) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        if (!VertxCommandServer.isStart || VertxCommandServer.selectDevicesWs.isEmpty()) {
            logW("服务器中未选中设备")
            return
        }
        executor.submit {
            VertxCommandServer.Command.getApplications({
                SwingUtilities.invokeLater {
                    val dialog = AppListDialog(project, it)
                    dialog.show()
                }
            })
        }


    }
}

private class AppListDialog(project: Project?, apps: ArrayList<ApplicationListPojo>) : DialogWrapper(project) {

    private val panel = JPanel().apply {
        // 获取应用列表(伪代码)
//        val apps = arrayListOf(ApplicationListPojo(name="ag"),ApplicationListPojo(name="agg"),ApplicationListPojo(name="aag"))
//        val apps = applicationListPojos

        // 创建表格模型
        val tableModel = DefaultTableModel(0, 0).apply {
            addColumn("图标")
            addColumn("应用名称")
            addColumn("包名")
            addColumn("安装时间")
            addColumn("版本名称")
            addColumn("版本代码")
//            addRow(arrayOf(ICONS.LOGO_16,"应用名称","包名","安装时间","版本名称","版本代码"))
            for (app in apps) {
                var image: ImageIcon? = null
                app.iconImage?.run {
                    image = ImageIcon(this)
                    if (image != null) {
                        val resizedImage: Image =
                            image!!.image.getScaledInstance(32, 32, Image.SCALE_DEFAULT)
                        // 创建一个新的 ImageIcon，使用调整大小后的图像
                        image = ImageIcon(resizedImage)
                    }
                }

                // 应用图标渲染器到第一列
                addRow(
                    arrayOf(
                        image ?: "null",
                        app.name,
                        app.packageName,
                        app.installTime,
                        app.versionName,
                        app.versionCode
                    )
                )
            }
        }

        // 创建表格
        val table = JBTable(tableModel)
        table.setDefaultRenderer(Any::class.java, IconTableCellRenderer())
        val pane = JScrollPane(table)
        add(pane)

        pane.preferredSize = Dimension(1200, 800)
        pane.revalidate()
        // 添加窗口大小修改监听
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                // 调整表格大小以适应窗口
                val newWidth = width - 20 // 调整为适当的大小，以适应窗口
                val newHeight = height - 20 // 调整为适当的大小，以适应窗口
                pane.preferredSize = Dimension(newWidth, newHeight)
                pane.revalidate()
            }
        })
    }

    init {
        init()
        title = "App List"
    }

    override fun createCenterPanel(): JComponent {
        return panel
    }
}

private class IconTableCellRenderer : DefaultTableCellRenderer() {
    override fun getTableCellRendererComponent(
        table: JTable?, value: Any?,
        isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int,
    ): Component {
        if (value is Icon) {
            val label = JLabel(value)
            label.horizontalAlignment = SwingConstants.CENTER
            label.verticalAlignment = SwingConstants.CENTER
            return label
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
    }
}