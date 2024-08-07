package github.zimo.autojsx.util

import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback
import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ResultHandler
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.io.File
import java.io.OutputStream

/**
 *
 * @author : zimo
 * @date : 2024/07/31
 */
object GradleUtils {

    fun refreshGradleProject(project: Project) {
        ExternalSystemUtil.refreshProject(
            project,
            GradleConstants.SYSTEM_ID,
            project.basePath ?: return,
            object : ExternalProjectRefreshCallback {
                override fun onSuccess(externalTaskId: ExternalSystemTaskId, externalProject: DataNode<ProjectData>?) {
                    super.onSuccess(externalTaskId, externalProject)
                    runGradleCommandOnToolWindow(project, "initEnvironment", "npmInstall")
                }
            },
            false,
            ProgressExecutionMode.START_IN_FOREGROUND_ASYNC,
        )
    }

    fun isGradleProject(project: Project): Boolean {
        val basePath = project.basePath ?: throw IllegalArgumentException("project.basePath must not be null")
        val file = File(basePath)
        return file.listFiles()?.count {
            it.name == "build.gradle" || it.name == "build.gradle.kts"
        }?.let {
            it > 0
        } ?: false
    }

    data class RunGradleCommandResult(val success: Boolean, val output: String, val error: String)

    fun runGradleCommand(project: Project, vararg command: String, callback: ((RunGradleCommandResult) -> Unit)? = null) {
        // 直接运行 Gradle 命令
        val connector = GradleConnector.newConnector().forProjectDirectory(project.basePath?.let { File(it) })
        connector.connect().use { connection ->
            val outputStream = StringBuilder()
            val errorStream = StringBuilder()
            connection.newBuild()
                .setStandardOutput(object : OutputStream() {
                    override fun write(b: Int) {
                        outputStream.append(b.toChar())
                    }
                }).setStandardError(object : OutputStream() {
                    override fun write(b: Int) {
                        errorStream.append(b.toChar())
                    }
                })
                .forTasks(*command)
                .run(object : ResultHandler<Void> {
                    override fun onComplete(p0: Void?) {
                        callback?.let { it(RunGradleCommandResult(true, outputStream.toString(), errorStream.toString())) }
                    }

                    override fun onFailure(p0: GradleConnectionException?) {
                        callback?.let { it(RunGradleCommandResult(false, outputStream.toString(), errorStream.toString())) }
                    }
                })
        }
    }

    fun runGradleCommandOnToolWindow(project: Project, vararg command: String, callback: ((Boolean) -> Unit)? = null) {
        // 让 Gradle 在构建窗口运行
        val settings = ExternalSystemTaskExecutionSettings()
        settings.externalProjectPath = project.basePath
        settings.taskNames = listOf<@NlsSafe String?>(*command)
        settings.externalSystemIdString = GradleConstants.SYSTEM_ID.toString()

        ExternalSystemUtil.runTask(
            settings,
            DefaultRunExecutor.EXECUTOR_ID,
            project,
            GradleConstants.SYSTEM_ID,
            object : TaskCallback {
                override fun onSuccess() {
                    callback?.let { it(true) }
                }

                override fun onFailure() {
                    callback?.let { it(false) }
                }
            },
            ProgressExecutionMode.START_IN_FOREGROUND_ASYNC,
            true
        )
    }
}

