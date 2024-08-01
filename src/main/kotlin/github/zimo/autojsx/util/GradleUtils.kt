package github.zimo.autojsx.util

import com.intellij.build.BuildViewManager
import com.intellij.build.DefaultBuildDescriptor
import com.intellij.build.events.impl.OutputBuildEventImpl
import com.intellij.build.events.impl.StartBuildEventImpl
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.gradle.toolingExtension.impl.util.GradleProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback
import com.intellij.openapi.externalSystem.service.project.manage.ExternalProjectsManagerImpl
import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.MessageBusConnection
import org.gradle.initialization.GradlePropertiesHandlingSettingsLoader
import org.gradle.internal.jvm.GradleVersionNumberLoader
import org.gradle.tooling.GradleConnector
import org.jetbrains.plugins.gradle.service.project.GradleProjectResolverUtil
import org.jetbrains.plugins.gradle.service.project.open.GradleProjectOpenProcessor
import org.jetbrains.plugins.gradle.settings.GradleSettings
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.jetbrains.plugins.gradle.util.GradleUtil
import java.io.File
import java.util.*

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
            object : ExternalProjectRefreshCallback{
                override fun onSuccess(externalTaskId: ExternalSystemTaskId, externalProject: DataNode<ProjectData>?) {
                    super.onSuccess(externalTaskId, externalProject)
                    runGradleCommandOnToolWindow(project,"npmInstall","initEnvironment")
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

    fun runGradleCommand(project: Project, vararg command: String) {
        // 直接运行 Gradle 命令
        val connector = GradleConnector.newConnector().forProjectDirectory(File(project.basePath))
        connector.connect().use { connection ->
            connection.newBuild()
                .forTasks(*command)
                .run()
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

