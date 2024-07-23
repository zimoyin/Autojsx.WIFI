package github.zimo.autojsx.action.news

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.modules
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.testFramework.HeavyPlatformTestCase.createChildData
import com.jetbrains.rd.util.getLogger
import com.jetbrains.rd.util.warn
import github.zimo.autojsx.icons.ICONS
import github.zimo.autojsx.module.MODULE_TYPE_ID
import github.zimo.autojsx.util.logI

/**
 * 新建项目的项目描述文件 json
 */
class NewAutoJSON :
    AnAction(github.zimo.autojsx.icons.ICONS.LOGO_16) {
    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file == null || !file.isDirectory) return
        //构建结构
        WriteCommandAction.runWriteCommandAction(e.project) {
            file.createChildData(this, "project.json").getOutputStream(this).use { outputStream ->
                outputStream.write(
                    """
                            |{
                            |    "name": "${file.name}",
                            |    "main": "main.js",
                            |    "ignore": [
                            |        "build"
                            |    ],
                            |    "launchConfig": {
                            |        "hideLogs": true
                            |    },
                            |    "packageName": "github.autojsx.${file.name}",
                            |    "versionName": "1.0.0",
                            |    "srcPath":"./../",
                            |    "resources":"./../",
                            |    "lib":"./../",
                            |    "versionCode": 1,
                            |    "obfuscator": false,
                            |    "obfuscatorPath": "./obfuscator.js"
                            |}
                        """.trimMargin().toByteArray()
                )
            }
            file.createChildData(this, "obfuscator.js").getOutputStream(this).use { outputStream ->
                ICONS::class.java.getResourceAsStream("/obfuscator.js")?.readAllBytes()?.let {
                    outputStream.write(it)
                }
            }
            // 刷新虚拟文件系统，以确保新创建的文件可见
            VirtualFileManager.getInstance().refreshWithoutFileWatcher(false)
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val moduleManager = ModuleManager.getInstance(project)
            val hasAutoJSXModule = moduleManager.modules.any { module ->
                ModuleType.get(module).id == MODULE_TYPE_ID
            }
            e.presentation.isEnabledAndVisible = hasAutoJSXModule
        } else {
            e.presentation.isEnabledAndVisible = false
        }
    }
}