package github.zimo.autojsx.module

import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import javax.swing.Icon

const val MODULE_TYPE_ID = "AUTO_JSX_MODULE_TYPE"
class MyModuleType : ModuleType<MyModuleBuilder>(MODULE_TYPE_ID) {


    fun getInstance(): MyModuleType {
        ModuleTypeManager.getInstance().findByID(MODULE_TYPE_ID)
        return ModuleTypeManager.getInstance().findByID(MODULE_TYPE_ID) as MyModuleType
    }


    override fun createModuleBuilder(): MyModuleBuilder {
        return MyModuleBuilder()
    }


    override fun getName(): String {
        return "Auto.JSX"
    }



    //这段文字会出现在鼠标悬浮提示中
    override fun getDescription(): String {
        return "Example custom module type"
    }


    //getNodeIcon() 应该返回模块类型特定的图标。
    override fun getNodeIcon(b: Boolean): Icon {
        return github.zimo.autojsx.icons.ICONS.LOGO_16
    }

//    override fun createWizardSteps(
//        wizardContext: WizardContext,
//        moduleBuilder: MyModuleBuilder,
//        modulesProvider: ModulesProvider,
//    ): Array<ModuleWizardStep?> {
//        return super.createWizardSteps(wizardContext, moduleBuilder, modulesProvider)
//    }

}