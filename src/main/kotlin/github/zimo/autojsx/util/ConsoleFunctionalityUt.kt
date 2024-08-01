package github.zimo.autojsx.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import github.zimo.autojsx.server.VertxCommand
import github.zimo.autojsx.server.VertxServer
import java.util.ArrayList
import java.util.HashMap
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JOptionPane
import javax.swing.JPanel

/**
 *
 * @author : zimo
 * @date : 2024/07/28
 */
fun selectDevice() {
    val map = HashMap<String, JCheckBox>()
    val panel = JPanel()
    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

    for (item in VertxServer.devicesWs.keys) {
        val checkBox = JCheckBox(item)
        map[item] = checkBox
        panel.add(checkBox)
    }

    VertxServer.selectDevicesWs.forEach {
        map[it.key]?.isSelected = true
    }


    val option = JOptionPane.showConfirmDialog(
        null, panel, "Selection Devices",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
    )

    if (option == JOptionPane.OK_OPTION) {
        val result = StringBuilder("Selection Devices:\n")
        for (component in panel.components) {
            if (component is JCheckBox) {
                val checkBox = component
                if (checkBox.isSelected) {
                    result.append("✔ ").append(checkBox.text).append("\n")
                } else {
                    result.append("  ").append(checkBox.text).append("\n")
                }
            }
        }
        JOptionPane.showMessageDialog(null, result.toString(), "Selection Devices", JOptionPane.INFORMATION_MESSAGE)

        VertxServer.selectDevicesWs.clear()
        map.forEach {
            if (it.value.isSelected) {
                VertxServer.devicesWs[it.key]?.apply {
                    VertxServer.selectDevicesWs[it.key] = this
                }
            }
        }
        logI("选中的设备为: ${VertxServer.selectDevicesWs.keys}")
    }
}

fun runningScriptList(project: Project) {
    if (!VertxServer.isStart || VertxServer.selectDevicesWs.isEmpty()) {
        logW("服务器中未选中设备")
        return
    }
    var result = false
    executor.submit {
        ProgressManager.getInstance().runProcessWithProgressSynchronously<Any, RuntimeException>(
            {
                while (true) {
                    Thread.sleep(600)
                    if (result) break
                }
            },
            "正在等待网络结果",
            true,  // indeterminate
            project
        )
    }
    executor.submit {
        runningScriptCheckBox {
            result = true
        }
    }
}

fun runningScriptCheckBox(callback: () -> Unit = {}) {
    logI("正在查询待关闭的脚本")

    VertxCommand.getRunningList({
        callback()
        Thread.sleep(600)
        val panel = JPanel()
        val list = ArrayList<JCheckBox>()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        if (it.isEmpty()) {
            val checkBox = JCheckBox("截止到当前没有任何脚本在运行")
            panel.add(checkBox)
        }
        for (item in it) {
            val checkBox = JCheckBox(item.sourceName)
            list.add(checkBox)
            panel.add(checkBox)
        }

        val option = JOptionPane.showConfirmDialog(
            null, panel, "Select Items",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        )

        if (option == JOptionPane.OK_OPTION) {
            val result = StringBuilder("Selected items:\n")
            for (component in panel.components) {
                if (component is JCheckBox) {
                    val checkBox = component
                    if (checkBox.isSelected) {
                        result.append("✔ ").append(checkBox.text).append("\n")
                    }
                }
            }
            JOptionPane.showMessageDialog(
                null,
                result.toString(),
                "Selection Result",
                JOptionPane.INFORMATION_MESSAGE
            )
            list.forEach {
                if (it.isSelected) {
                    VertxCommand.stopScriptBySourceName(it.text)
                }
            }
        }
    })
    callback()
}