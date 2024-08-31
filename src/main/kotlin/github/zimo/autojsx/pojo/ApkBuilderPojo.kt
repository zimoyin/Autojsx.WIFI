package github.zimo.autojsx.pojo

import github.zimo.autojsx.util.logI
import java.io.File
import java.nio.file.Files

/**
 *
 * @author : zimo
 * @date : 2024/08/28
 */
data class ApkBuilderPojo(
    /**
     * 是否使用 GUI
     * 如果不使用则直接进行打包，启用后可以详细的设置打包
     */
    val gui: Boolean = true,

    /**
     * 综合资源后的路径，代表 GRADLE 编译后的，代表 AUTOJS 混合后的项目
     */
    val assets: List<String> = emptyList(),

    /**
     * 项目的 project.json，如果没有则让工具生成
     */
    val projectJson: String? = null,

    /**
     * 工作目录
     */
    var workDir: String? = null,

    /**
     * 图标路径
     */
    val iconPath: String? = null,

    /**
     * 开屏图片
     */
    val startIconPath: String? = null,

    /**
     * 模板 apk 路径
     */
    val templateApkPath: String? = null,

    /**
     * 签名文件
     */
    val signatureFile: String? = null,

    /**
     * 签名别名
     */
    val signatureAlias: String? = null,
    /**
     * 签名密码
     */
    val signaturePassword: String? = null,
) {
    init {
        if (workDir == null) workDir = Files.createTempDirectory("autox_apk_builder").toFile().apply {
            deleteOnExit()
        }.absolutePath
        deleteCache()
    }

    fun deleteCache(){
        File(workDir,"template").delete()
        File(workDir,"decode").delete()
        File(workDir,"centralizedAssets").delete()
    }

    fun centralizedAssetsFile() = File(workDir!!, "centralizedAssets")

    fun centralizedAssets(): String {
        val file = centralizedAssetsFile()
        file.delete()
        if (!file.exists()) file.mkdirs()
        for (asset in assets) {
            kotlin.runCatching {
                copy(File(asset),file)
            }.onFailure {
                logI("[ERROR] ${it.message}")
            }
        }

        return File(workDir!!, "centralizedAssets").apply { if (!exists()) mkdirs() }.absolutePath
    }

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