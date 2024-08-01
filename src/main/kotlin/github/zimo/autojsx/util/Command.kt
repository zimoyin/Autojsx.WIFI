package github.zimo.autojsx.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import com.intellij.openapi.vfs.readText
import github.zimo.autojsx.icons.ICONS
import github.zimo.autojsx.util.GradleUtils.isGradleProject
import io.vertx.core.json.JsonObject
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * 压缩项目
 * @param file 选择的文件
 * @param project 项目
 */
fun zipProject(
    file: VirtualFile,
    project: Project?,
): ZipProjectResult {
    val info = ZipProjectJsonInfo.findProjectJsonInfo(file, project)

    if (info == null) logE("无法压缩文件夹，该文件夹不是一个项目文件夹: 无法找到 project.json 文件")

    val paths = ArrayList<String>().apply {
        info?.lib?.canonicalPath?.let { add(it) }
        info?.resources?.canonicalPath?.let { add(it) }
        info?.src?.canonicalPath?.let { add(it) }
        // 检测是否是 Gradle 项目
        if (project != null && isGradleProject(project)) {
            // 没有 src 的情况，并且项目是 Gradle 项目。放入根目录
            if (info?.src == null) {
                add(File(file.path).canonicalPath)
            } else {
                logW("你正在 Gradle 构建的 Kotlin/Js 环境下执行，Autojs 原生项目")
            }
        }

    }

    return ByteArrayOutputStream().let {
        zip(paths, null, it)
        return@let ZipProjectResult(info ?: ZipProjectJsonInfo(File(file.path)), it.toByteArray())
    }
}

data class ZipProjectResult(
    val info: ZipProjectJsonInfo,
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ZipProjectResult

        if (info != other.info) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = info.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

data class ZipProjectJsonInfo(
    val projectJson: File,
    val src: File? = null,
    val resources: File? = null,
    val lib: File? = null,
    val name: String = "Debug",
) {
    companion object {
        fun from(projectJson: File, project: Project?): ZipProjectJsonInfo {
            val json = JsonObject(projectJson.readText())
            return ZipProjectJsonInfo(
                projectJson = projectJson,
                src = projectJson.resolve(json.getString("srcPath") ?: "autojs_null_paeg85132eg").let {
                    if (it.exists()) it else null
                },
                resources = projectJson.resolve(json.getString("resources") ?: "autojs_null_paeg85132eg").let {
                    if (it.exists()) it else null
                },
                lib = projectJson.resolve(json.getString("lib") ?: "autojs_null_paeg85132eg").let {
                    if (it.exists()) it else null
                },
                name = json.getString("name") ?: project?.name ?: "Debug",
            )
        }

        fun findProjectJsonInfo(file: VirtualFile, project: Project?): ZipProjectJsonInfo? {
            if (file.isDirectory && file.children.isEmpty()) return null
            if (file.isFile && file.name == "project.json")
                return runCatching { from(File(file.path), project) }.getOrNull()
            if (file.isFile && file.name != "project.json") return null
            val projectJson = file.let {
                if (it.isDirectory) findFile(it, "project.json") else it
            }?.let {
                if (!it.exists()) null else File(it.path)
            }.let {
                it
                    ?: throw IllegalArgumentException("无法压缩文件夹，该文件夹不是一个项目文件夹: 无法找到 project.json 文件")
            }

            return runCatching { from(projectJson, project) }.getOrNull()
        }
    }

    fun isAutoJsProject(): Boolean {
        return src != null && resources != null && lib != null
    }
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