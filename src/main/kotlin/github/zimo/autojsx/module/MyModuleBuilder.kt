package github.zimo.autojsx.module

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.util.GradleUtils
import github.zimo.autojsx.util.findOrCreateDirectory
import github.zimo.autojsx.util.writeText
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream
import javax.swing.JComponent
import javax.swing.JLabel


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
            if (values.containsValue("Kotlin/Js")) entry.file?.apply {
                createKotlinAndJs(this)
                this.findOrCreateChildData(this, "settings.gradle.kts").writeText("""
                    plugins {
                        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
                    }
                    rootProject.name = "${entry.file?.name}"
                """.trimIndent())
                this.findOrCreateDirectory("src").apply {
                    findOrCreateDirectory("jsMain").apply {
                        findOrCreateDirectory("resources").apply {
                            createChildData(this, "project.json").writeText(
                                """
                                {
                                    "name": "${entry.file?.name}",
                                    "main": "main.js",
                                    "ignore": [
                                        "build"
                                    ],
                                    "launchConfig": {
                                        "hideLogs": false
                                    },
                                    "packageName": "github.autojsx.${entry.file?.name}",
                                    "versionName": "1.0.0",
                                    "versionCode": 1,
                                    "obfuscator": false
                                }
                                """.trimIndent()
                            )
                        }
                    }
                }
                GradleUtils.refreshGradleProject(modifiableRootModel.project)
            } else {
                // 空项目：留空
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
        return super.createProject(name, path)
    }

    private fun createKotlinAndJs(file: VirtualFile) {
        val buffer = ByteArray(1024)
        MyModuleBuilder::class.java.classLoader.getResourceAsStream("KotlinAndJs.zip")?.apply {
            try {
                // 打开zip文件流
                val zipInputStream = ZipInputStream(this)

                // 逐个解压zip条目
                var zipEntry = zipInputStream.nextEntry
                while (zipEntry != null) {
                    val unzipFilePath = file.path + File.separator + zipEntry.name

                    // 如果条目是文件则创建文件
                    if (zipEntry.isDirectory) {
                        File(unzipFilePath).mkdirs()
                    } else {
                        File(unzipFilePath).parentFile.mkdirs()
                        val fileOutputStream = FileOutputStream(unzipFilePath)
                        var len: Int
                        while (zipInputStream.read(buffer).also { len = it } > 0) {
                            fileOutputStream.write(buffer, 0, len)
                        }
                        fileOutputStream.close()
                    }
                    zipEntry = zipInputStream.nextEntry
                }
                zipInputStream.closeEntry()
                zipInputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}