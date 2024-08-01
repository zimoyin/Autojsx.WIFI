package github.zimo.autojsx.module

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent


/**
 * 每一个 ModuleWizardStep 就是创建项目的一个步骤，当然最后一步 finish 那页不算
 */
class MyModuleWizardStep(private val values: HashMap<String, String>) : ModuleWizardStep() {
    override fun getComponent(): JComponent {
        //IDEA 还提供了另一套 Swing 组件（JB-前缀）
        var selected = false
        // 创建单选标签
        val root = panel {
            buttonsGroup {
                indent {
                    rowsRange {
                        row {
                            radioButton("Kotlin/Js: 使用Kotlin 来编译 Autojs 所需的 js 文件,适用于 Kotlin 脚本编写",true)
                                .bindSelected({selected},{selected = it})
                                .actionListener { _, _ ->
                                    values["项目结构"] = "Kotlin/Js"
                                }
                            bottomGap(BottomGap.MEDIUM)
                        }
                        row {
                            radioButton("项目组: 没有任何提示的空项目,适用于 Js 脚本编写",false)
                                .bindSelected({selected},{selected = it})
                                .actionListener { _, _ ->
                                    values["项目结构"] = "空项目"
                                }
                            bottomGap(BottomGap.MEDIUM)
                        }
                    }
                }
            }.bind({ selected }, { selected = it })
        }

        return root
    }

    override fun updateDataModel() {
        //todo update model according to UI
    }
}