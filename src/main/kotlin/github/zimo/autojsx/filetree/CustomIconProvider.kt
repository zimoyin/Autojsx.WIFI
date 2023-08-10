package github.zimo.autojsx.filetree

import com.intellij.ide.IconProvider
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import github.zimo.autojsx.icons.ICONS
import javax.swing.Icon


class CustomIconProvider : IconProvider() {
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        if (element is PsiDirectory) {
            val project = element.project
            val dirFile = element.virtualFile
            val fileIndex = ProjectFileIndex.getInstance(project)
            if (project.basePath != dirFile.path) // 防止替换根项目图标
                if (dirFile.isDirectory && fileIndex.isInContent(dirFile) && !fileIndex.isInSourceContent(dirFile)) {
                    // Check if the folder contains "src" folder
                    val srcFolder = dirFile.findChild("src")
                    // Check if the folder contains "resources" folder
                    val resourcesFolder = dirFile.findChild("resources")
                    if (srcFolder != null && srcFolder.isDirectory && resourcesFolder != null && resourcesFolder.isDirectory) {
                        // Check if "resources" folder contains "test.txt" file
                        val testFile = resourcesFolder.findChild(SEARCH_FILE_NAME)
                        if (testFile != null && !testFile.isDirectory) {
                            // Replace the folder icon with your custom icon
                            // Make sure to put your custom icon in the resources folder of your plugin
                            return ICONS.PROJECT_16
                        }
                    }
                }
        } else if (element is PsiFile) {
            val project = element.project
            val fileIndex = ProjectFileIndex.getInstance(project)
            // Handle file icons here if needed
            if (element.parent?.name == "resources" && element.name == "project.json" &&
                fileIndex.isInContent(element.virtualFile) && !fileIndex.isInSourceContent(element.virtualFile)
            ) {
                return ICONS.CONFIG_16
            }
        }

        // Return null to use the default icon
        return null
    }

    companion object {
        private const val SEARCH_FILE_NAME = "test.txt"
    }
}
