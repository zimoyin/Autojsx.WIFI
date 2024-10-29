package github.zimo.autojsx.action.command

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.HttpRequests
import com.jetbrains.rd.util.string.printToString
import github.zimo.autojsx.action.run.doc.DocRunProjectButton
import github.zimo.autojsx.pojo.ApkBuilderPojo
import github.zimo.autojsx.server.ConsoleOutput
import github.zimo.autojsx.util.*
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.runBlocking
import org.gradle.internal.impldep.bsh.commands.dir
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.SystemIndependent
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URLClassLoader
import java.nio.charset.Charset
import javax.swing.SwingUtilities
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 *
 * @author : zimo
 * @date : 2024/08/25
 */
class ApkBuilder :
    AnAction("打包应用", "", null) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        executor.submit { buildApk(project) }
    }

    companion object {
        private const val url = "https://api.github.com/repos/zimoyin/autox_apk_builder/releases/latest"
        fun buildApk(project: Project) {
            logW("打包APK目前仅支持从当前打开的文件查找项目")
            val base = (project.basePath ?: (project.projectFile
                ?: project.workspaceFile)?.parent?.parent?.canonicalPath).apply {
                if (this == null) {
                    logE("无法获取到项目根路径: ${project.basePath}")
                    return
                } else {
                    logI("项目根路径: $this")
                }
            }
            val dir = File(base, ".gradle").apply { mkdirs() }
            val coreJar = File(dir, "autox_apk_builder.jar")
            val workDir = File(dir, "apk_build_cache").apply { mkdirs() }
            val workAssetDir = File(workDir, "asset").apply { if (exists()) deleteRecursively();mkdirs(); }
            val projectJson = File(workAssetDir, "project.json").apply { mkdirs() }

            val fileEditorManager = project.fileEditorManager()
            fileEditorManager.saveCurrentDocument()
            val selectedFile = fileEditorManager.selectedFiles.firstOrNull()

            val isGradleProject = GradleUtils.isGradleProject(project)

            // download core jar
            val downloadTask = executor.submit { downloadCoreJar(coreJar) }



            executor.execute {
                kotlin.runCatching {
                    // 构建 asset; 构建完成后会将内容复制到 workAssetDir
                    buildAsset(selectedFile, isGradleProject, project, base, workAssetDir)
                    downloadTask.get()

                    val pojo = ApkBuilderPojo(
                        workDir = workDir.absolutePath,
                        assets = listOf(workAssetDir.absolutePath),
                        projectJson = projectJson.absolutePath
                    )
                    val config = File(dir, "apk_builder_config.json").apply {
                        val json = if (this.exists()) {
                            JsonObject(readText()).apply {
                                put("workDir", pojo.workDir)
                                put("assets", pojo.assets)
                                put("projectJson", pojo.projectJson)
                            }
                        } else {
                            JsonObject.mapFrom(pojo)
                        }
                        writeText(json.printToString())
                    }

                    val javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"
                    val jarPath = coreJar.absolutePath

                    logI("修改配置文件可以让界面无窗体启动")
                    val processBuilder = ProcessBuilder(
                        javaBin, "-cp", jarPath, "com.github.zimoyin.autox.gui.MainKt", config.absolutePath,
                    )

                    logI("开始运行第三方打包程序")
                    logW("如果无法运行,请到: https://github.com/zimoyin/Autojsx.WIFI")
                    logW("如果打包程序出现BUG,请到：https://github.com/zimoyin/autox_apk_builder")
                    logI("Java Path: $javaBin")
                    logI("WorkDir Path: $workDir")
                    logI("Config Path: $config")
                    logI(processBuilder.command().joinToString(" "))
                    val process = processBuilder.start()

                    executor.submit {
                        val reader = BufferedReader(InputStreamReader(process.inputStream, Charset.forName("GBK")))
                        reader.forEachLine {
                            ConsoleOutput.systemPrint(it) // 读取标准输出并打印日志
                        }
                    }

                    executor.submit {
                        val reader = BufferedReader(InputStreamReader(process.errorStream, Charset.forName("GBK")))
                        reader.forEachLine {
                            ConsoleOutput.systemPrint(it) // 读取错误输出并打印日志
                        }
                    }

                    process.waitFor()

                }.onFailure {
                    logE("打包失败: ${it.message}", it)
                }
            }

        }

        private fun buildAsset(
            selectedFile: VirtualFile?,
            isGradleProject: Boolean,
            project: Project,
            base: @SystemIndependent @NonNls String?,
            workAssetDir: File
        ) {
            if (selectedFile == null) {
                if (!isGradleProject) {
                    logW("请打开一个AutoJs项目中的文件，程序将以此为基础搜索项目")
                    throw IllegalStateException("当前没有打开任何文件,无法确定打包位置")
                }
                gradleProjectBuildAsset(project, base, workAssetDir)
            } else {
                val isAutoJsProject = autojsProjectBuildAsset(selectedFile, project, workAssetDir)
                if (!isAutoJsProject && isGradleProject) {
                    gradleProjectBuildAsset(project, base, workAssetDir)
                }
                if (!isAutoJsProject && !isGradleProject) {
                    logW("请打开一个正确的AutoJs项目并打开其中的文件，程序将以此为基础搜索项目")
                    throw IllegalArgumentException("无法找到AutoJs项目，且当前项目不是Gradle项目，无法打包")
                }
            }
        }

        private fun downloadCoreJar(coreJar: File) {
            if (coreJar.exists()) {
                if (coreJar.isDirectory && !coreJar.isFile) {
                    coreJar.delete()
                    coreJar.mkdirs()
                } else {
                    if (coreJar.length() > 20 * 1024 * 1024) {
                        logI("检测到打包文件存在, 不需要重新下载。如果需要重新下载请手动删除: $coreJar")
                        return
                    }
                }
            }
            kotlin.runCatching {
                var browser_download_url: String? = null
                HttpRequests.request(url).connect { request ->
                    val json = request.readString()
                    val jsonObject = JsonObject(json)
                    jsonObject.getJsonArray("assets").forEach {
                        if (browser_download_url != null) return@forEach
                        val j1 = JsonObject(it.toString())
                        if (!j1.getString("name").startsWith("autox_apk_builder")) return@forEach
                        browser_download_url = j1.getString("browser_download_url")
                    }
                    logI("获取到最新的下载地址：$browser_download_url")
                    val burl = browser_download_url ?: throw NullPointerException("无法获取打包文件最新版本")
                    HttpRequests.request(burl).connect { request2 ->
                        request2.inputStream.use { input ->
                            coreJar.outputStream().use { output ->
                                input.copyTo(output)
                                logI("下载打包文件成功: $coreJar")
                            }
                        }
                    }
                }
            }.onFailure {
                logW("下载打包文件失败，请在IDEA 设置 代理后重试")
                throw IllegalStateException("下载打包文件失败", it)
            }
        }

        private fun autojsProjectBuildAsset(
            selectedFile: VirtualFile?,
            project: Project,
            workAssetDir: File
        ): Boolean {
            // 向上查询 src 目录的父目录
            var findFile: VirtualFile? = selectedFile
            for (i in 0 until 6) {
                if (findFile == null) break
                findFile = findFile.parent

                if (findFile == null || findFile.canonicalPath == project.basePath || findFile.findDirectory("src") != null) {
                    break
                }
            }

            var isAutoJsProject = false
            if (findFile != null) {
                ZipProjectJsonInfo.findProjectJsonInfo(findFile, project)?.apply {
                    if (!isAutoJsProject()) return@apply
                    resources?.let { cp(it, workAssetDir) }
                    lib?.let { cp(it, workAssetDir) }
                    src?.let { cp(it, workAssetDir) }
                    logI("拷贝resources到asset目录: \n$findFile \n  ⬇ \n$workAssetDir\n")
                    logI("拷贝lib到asset目录: \n$lib \n  ⬇ \n$workAssetDir\n")
                    logI("拷贝src到asset目录: \n$src \n  ⬇ \n$workAssetDir\n")
                    if (workAssetDir.list()?.firstOrNull { it == "sdk" } != null){
                        workAssetDir.resolve("sdk").deleteRecursively()
                        workAssetDir.resolve("sdk").delete()
                        logI("删除sdk目录: ${workAssetDir.resolve("sdk")}")
                    }
                    isAutoJsProject = true
                }
            }
            return isAutoJsProject
        }

        private fun gradleProjectBuildAsset(
            project: Project,
            base: @SystemIndependent @NonNls String?,
            workAssetDir: File
        ) {
//            DocRunProjectButton.runBackgroundGradleProject(project, false)
            callRunGradleProject(project)
            val file = File(base, "build/autojs/intermediate_compilation_files")
            copy(file, workAssetDir)
            logI("拷贝intermediate_compilation_files到asset目录: \n$file \n  ⬇ \n$workAssetDir")
        }

        // 将挂起函数封装为 普通函数
        private fun callRunGradleProject(project: Project): Boolean = runBlocking {
            runGradleProject(project)
        }

        // 将异步方法包装成挂起函数
        private suspend fun runGradleProject(project: Project): Boolean = suspendCoroutine { cont ->
            DocRunProjectButton.runBackgroundGradleProject(project, false) { result ->
                cont.resume(result)
            }
        }

        /**
         * 拷贝文件到目标文件夹
         * @param destination 目标文件夹
         * @param name 如果 this 是文件的话，则重命名
         */
        fun File.copyDest(destination: File, name: String? = null) {
            github.zimo.autojsx.action.command.ApkBuilder.Companion.copy(this, destination, name)
        }

        private fun cp(source: File, destination: File, name: String? = null) {
            copy(source, destination, name)
        }

        /**
         * 拷贝文件到目标文件夹
         * @param source 源文件夹/文件
         * @param destination 目标文件夹
         * @param name 如果 source 是文件的话，则重命名
         */
        private fun copy(source: File, destination: File, name: String? = null) {
            if (!source.exists()) {
                throw IllegalArgumentException("Source directory doesn't exist: ${source.absolutePath}")
            }

            if (!destination.exists()) {
                destination.mkdirs()
            }

            if (source.isFile || !source.isDirectory) {
                val destFile = File(destination, name ?: source.name)
                source.copyTo(destFile, overwrite = true)
                return
            }

            source.listFiles()?.forEach { file ->
                val destFile = File(destination, file.name)
                if (file.isDirectory) {
                    copy(file, destFile)
                } else {
                    file.copyTo(destFile, overwrite = true)
                }
            }
        }
    }
}