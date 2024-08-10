package github.zimo.autojsx.window.hierarchy

import com.intellij.openapi.ui.DialogWrapper
import github.zimo.autojsx.util.logI
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import javax.swing.*


class GeneratingCode(val hierarchy: HierarchyState) {

    fun generateCode() {
        val dialog = object : DialogWrapper(true) {
            private lateinit var idCheckBox: JCheckBox
            private lateinit var textCheckBox: JCheckBox
            private lateinit var classNameCheckBox: JCheckBox
            private lateinit var packageNameCheckBox: JCheckBox
            private lateinit var applicationPackageNameCheckBox: JCheckBox
            private lateinit var descCheckBox: JCheckBox
            private lateinit var boundsCheckBox: JCheckBox
            private lateinit var checkableCheckBox: JCheckBox
            private lateinit var checkedCheckBox: JCheckBox
            private lateinit var clickableCheckBox: JCheckBox
            private lateinit var enabledCheckBox: JCheckBox
            private lateinit var focusableCheckBox: JCheckBox
            private lateinit var focusedCheckBox: JCheckBox
            private lateinit var scrollableCheckBox: JCheckBox
            private lateinit var longClickableCheckBox: JCheckBox
            private lateinit var passwordCheckBox: JCheckBox
            private lateinit var selectedCheckBox: JCheckBox

            init {
                init()
                title = "生成代码选项"
            }

            override fun createCenterPanel(): JComponent? {
                val panel = JPanel()
                panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

                idCheckBox = JCheckBox("使用 ID")
                textCheckBox = JCheckBox("使用 Text")
                classNameCheckBox = JCheckBox("使用 ClassName")
                packageNameCheckBox = JCheckBox("使用 PackageName")
                applicationPackageNameCheckBox = JCheckBox("使用 ApplicationPackageName")
                descCheckBox = JCheckBox("使用 ContentDesc")
                boundsCheckBox = JCheckBox("使用 Bounds")
                checkableCheckBox = JCheckBox("使用 Checkable")
                checkedCheckBox = JCheckBox("使用 Checked")
                clickableCheckBox = JCheckBox("使用 Clickable")
                enabledCheckBox = JCheckBox("使用 Enabled")
                focusableCheckBox = JCheckBox("使用 Focusable")
                focusedCheckBox = JCheckBox("使用 Focused")
                scrollableCheckBox = JCheckBox("使用 Scrollable")
                longClickableCheckBox = JCheckBox("使用 LongClickable")
                passwordCheckBox = JCheckBox("使用 Password")
                selectedCheckBox = JCheckBox("使用 Selected")

                panel.add(idCheckBox)
                panel.add(textCheckBox)
                panel.add(classNameCheckBox)
                panel.add(packageNameCheckBox)
                panel.add(applicationPackageNameCheckBox)
                panel.add(descCheckBox)
                panel.add(boundsCheckBox)
                panel.add(checkableCheckBox)
                panel.add(checkedCheckBox)
                panel.add(clickableCheckBox)
                panel.add(enabledCheckBox)
                panel.add(focusableCheckBox)
                panel.add(focusedCheckBox)
                panel.add(scrollableCheckBox)
                panel.add(longClickableCheckBox)
                panel.add(passwordCheckBox)
                panel.add(selectedCheckBox)

                return panel
            }

            fun isIDSelected(): Boolean = idCheckBox.isSelected
            fun isTextSelected(): Boolean = textCheckBox.isSelected
            fun isClassNameSelected(): Boolean = classNameCheckBox.isSelected
            fun isPackageNameSelected(): Boolean = packageNameCheckBox.isSelected
            fun isApplicationPackageNameSelected(): Boolean = applicationPackageNameCheckBox.isSelected
            fun isDescSelected(): Boolean = descCheckBox.isSelected
            fun isBoundsSelected(): Boolean = boundsCheckBox.isSelected
            fun isCheckableSelected(): Boolean = checkableCheckBox.isSelected
            fun isCheckedSelected(): Boolean = checkedCheckBox.isSelected
            fun isClickableSelected(): Boolean = clickableCheckBox.isSelected
            fun isEnabledSelected(): Boolean = enabledCheckBox.isSelected
            fun isFocusableSelected(): Boolean = focusableCheckBox.isSelected
            fun isFocusedSelected(): Boolean = focusedCheckBox.isSelected
            fun isScrollableSelected(): Boolean = scrollableCheckBox.isSelected
            fun isLongClickableSelected(): Boolean = longClickableCheckBox.isSelected
            fun isPasswordSelected(): Boolean = passwordCheckBox.isSelected
            fun isSelectedSelected(): Boolean = selectedCheckBox.isSelected
        }

        // 显示对话框并等待用户输入
        if (dialog.showAndGet()) {
            val isID = dialog.isIDSelected()
            val isText = dialog.isTextSelected()
            val isClassName = dialog.isClassNameSelected()
            val isPackageName = dialog.isPackageNameSelected()
            val isApplicationPackageName = dialog.isApplicationPackageNameSelected()
            val isDesc = dialog.isDescSelected()
            val isBounds = dialog.isBoundsSelected()
            val isCheckable = dialog.isCheckableSelected()
            val isChecked = dialog.isCheckedSelected()
            val isClickable = dialog.isClickableSelected()
            val isEnabled = dialog.isEnabledSelected()
            val isFocusable = dialog.isFocusableSelected()
            val isFocused = dialog.isFocusedSelected()
            val isScrollable = dialog.isScrollableSelected()
            val isLongClickable = dialog.isLongClickableSelected()
            val isPassword = dialog.isPasswordSelected()
            val isSelected = dialog.isSelectedSelected()

            val node = hierarchy.selectNode
            val code = """
                selector()
                ${if (isID) """.id("${node?.id}")""" else ""}
                ${if (isText) """.text("${node?.text}")""" else ""}
                ${if (isClassName) """.className("${node?.className}")""" else ""}
                ${if (isPackageName) """.packageName("${node?.packageName}")""" else ""}
                ${if (isApplicationPackageName) """.applicationPackageName("${node?.applicationPackageName}")""" else ""}
                ${if (isDesc) """.desc("${node?.desc}")""" else ""}
                ${if (isBounds) """.boundsInScreen("${node?.bounds?.left}", "${node?.bounds?.top}", "${node?.bounds?.right}", "${node?.bounds?.bottom}")""" else ""}
                ${if (isCheckable) """.checkable("${node?.checkable}")""" else ""}
                ${if (isChecked) """.checked("${node?.checked}")""" else ""}
                ${if (isClickable) """.clickable("${node?.clickable}")""" else ""}
                ${if (isEnabled) """.enabled("${node?.enabled}")""" else ""}
                ${if (isFocusable) """.focusable("${node?.focusable}")""" else ""}
                ${if (isFocused) """.focused("${node?.focused}")""" else ""}
                ${if (isScrollable) """.scrollable("${node?.scrollable}")""" else ""}
                ${if (isLongClickable) """.longClickable("${node?.longClickable}")""" else ""}
                ${if (isPassword) """.password("${node?.password}")""" else ""}
                ${if (isSelected) """.selected("${node?.selected}")""" else ""}
            """.trimIndent()
            var finalCode: String = ""
            code.lines().forEach {
                if (it.trim().isNotEmpty()) finalCode += it
            }
            kotlin.runCatching {
                setSysClipboardText(finalCode)
                logI("代码已经复制到了剪切板")
            }
            StringDisplayDialog(finalCode).show()
        }
    }

    /**
     * 将字符串复制到剪切板。
     */
    fun setSysClipboardText(writeMe: String?) {
        val clip: Clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()
        val tText: Transferable = StringSelection(writeMe)
        clip.setContents(tText, null)
    }
    class StringDisplayDialog(private val message: String) : DialogWrapper(true) {

        init {
            init()
            title = "代码以生成并放入剪切板"
        }

        override fun createCenterPanel(): JComponent {
            val panel = JPanel()
            val label = JTextArea(message)
            panel.add(label)
            return panel
        }
    }
}
