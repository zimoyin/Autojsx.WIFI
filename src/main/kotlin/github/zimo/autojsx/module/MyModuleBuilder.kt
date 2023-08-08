package github.zimo.autojsx.module

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.externalSystem.model.project.LibraryLevel
import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.util.createSDK
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream


/**
 * 创建自定义模块生成器
 */
//ModuleBuilder
class MyModuleBuilder : ModuleBuilder() {
    val values: HashMap<String, String> = HashMap()

    init {
        addListener {

        }
    }

    /**
     * 提供要添加到向导中的自定义 UI 组件的实现。在这种情况下，请将其保留为标签。
     */
//    override fun createWizardSteps(
//        wizardContext: WizardContext,
//        modulesProvider: ModulesProvider,
//    ): Array<ModuleWizardStep> {
//        return arrayOf(object : ModuleWizardStep() {
//            override fun getComponent(): JComponent {
//                return JLabel("我的内容放在此处")
//            }
//
//            override fun updateDataModel() {}
//        })
//    }

    // 通过覆盖为新模块设置根模型
    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
        doAddContentEntry(modifiableRootModel)?.let { entry ->
            if (values.containsValue("SDK 单项目")) entry.file?.apply {
                createChildDirectory(this, "lib").apply {
                    createSDK(this)
                }
                createChildDirectory(this, "src").apply {
                    entry.addSourceFolder(this, true)
                }
            }

        }
    }



    override fun isAvailable(): Boolean {
        return true
    }

    //获取模块类型
    override fun getModuleType(): MyModuleType {
        //设置要提供的额外向导步骤的模块类型。在此示例中，选择EMPTY模块类型。
        return MyModuleType().getInstance()
//        return ModuleType.EMPTY
    }


    /**
     * 要在第一个向导页面上显示的自定义用户界面
     */
    override fun getCustomOptionsStep(context: WizardContext?, parentDisposable: Disposable?): ModuleWizardStep {
        return MyModuleWizardStep(values)
    }

    override fun commitModule(project: Project, model: ModifiableModuleModel?): Module? {
        val module = super.commitModule(project, model)
        println("pro: $project")
        return module
    }

    override fun createModule(moduleModel: ModifiableModuleModel): Module {
        return super.createModule(moduleModel)
    }


    override fun createProject(name: String?, path: String?): Project? {
        println("createProject name: $name path: $path exists:${path?.let { File(it).exists() }}")
        return super.createProject(name, path)
    }

}