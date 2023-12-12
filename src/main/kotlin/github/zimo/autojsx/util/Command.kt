package github.zimo.autojsx.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.icons.ICONS
import io.vertx.core.json.JsonObject
import java.io.File


inline fun zipProject(file: VirtualFile, project: Project?, crossinline zipCaller: (AutoJsProjectInfo) -> Unit) {
    val projectJSON = "project.json"
    var jsonFile: VirtualFile? = null
    jsonFile = if (file.isDirectory) {
        findFile(file, projectJSON)
    } else {
        file
    }
    if (jsonFile != null && jsonFile.exists()) {
        val projectJson = File(jsonFile.path)
        val json = JsonObject(projectJson.readText())
        runServer(project)
        val projectName = json.getString("name") ?: project?.name ?: "none"
        val isObfuscator = json.getBoolean("obfuscator") ?: false

        val outputPath = project?.basePath + File.separator + "build-output"
        val name = json.getString("name")
        var src = projectJson.resolve(json.getString("srcPath")).canonicalFile
        // 检查 resources 下是否有 obfuscator.json 文件
        val obfuscatorFile = File(jsonFile.parent.path).resolve("obfuscator.js")
        if (!obfuscatorFile.exists()) {
            obfuscatorFile.createNewFile()
            ICONS::class.java.getResourceAsStream("/obfuscator.js")?.readAllBytes()
                ?.let { obfuscatorFile.writeBytes(it) }
        }
        // 释放 node_modules.zip
        if (isObfuscator) {
            val modules = File(outputPath + File.separator + "obfuscator" + File.separator + "lib")
            if (!modules.exists()) ICONS::class.java.getResourceAsStream("/node_modules.zip")?.unzip(modules)
            // 创建临时混淆目录
            src = dirObfuscator(
                src,
                outputPath + File.separator + "obfuscator" + File.separator + projectName,
                obfuscatorFile,
                modules
            )
        }
        val resources = projectJson.resolve(json.getString("resources")).canonicalFile
        val lib = projectJson.resolve(json.getString("lib")).canonicalFile

        val zip = File(project?.basePath + "/build-output" + "/${name}.zip")
        zip.parentFile.mkdirs()
        if (zip.exists()) zip.delete()

        executor.submit {
            zip(
                arrayListOf(src.path, resources.path, lib.path),
                outputPath + File.separator + "${name}.zip"
            )
            zipCaller(
                AutoJsProjectInfo(
                    zip.canonicalPath,
                    projectJson.path,
                    src.path,
                    resources.path,
                    lib.path,
                    name,
                    false
                )
            )
        }
        return
    }
    logE("项目无法压缩: 选择的文件夹没有包含项目描述文件 'project.json'")
}

fun dirObfuscator(src: File, outputPath: String, obfuscatorFile0: File, modules0: File): File {
    val obfuscator = File(outputPath)
    obfuscator.delete()
    obfuscator.mkdirs()
    if (src.isDirectory) src.listFiles()?.forEach { file ->
        if (file.isDirectory) {
            dirObfuscator(file, outputPath, obfuscatorFile0, modules0)
        } else {
            if (file.name.endsWith(".js")) {
                val obfuscatorPath = obfuscator.canonicalPath
                val obfuscatorFile = File(obfuscatorPath + File.separator + file.name)
                if (!obfuscatorFile.exists()) {
                    obfuscatorFile.createNewFile()
                }
                obfuscatorFile.writeText(obfuscate(file.readText(), obfuscatorFile.path, obfuscatorFile0, modules0)!!)
            }
        }
    }
    return obfuscator
}

data class AutoJsProjectInfo(
    val zipPath: String,
    val projectJsonPath: String,
    val srcPath: String,
    val resourcesPath: String,
    val libPath: String,
    val name: String,
    val isConfusing: Boolean,
)