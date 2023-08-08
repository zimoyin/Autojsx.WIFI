package github.zimo.autojsx.filetree

import com.intellij.ide.FileIconProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import github.zimo.autojsx.icons.ICONS
import javax.swing.Icon


/**
 * 替换 autojs 的配置文件图标
 */
class CustomFileIconProvider : FileIconProvider {

    override fun getIcon(file: VirtualFile, flags: Int, project: Project?): Icon? {
        if (file.parent.name == "resources" && file.name == "project.json") {
            return ICONS.CONFIG_16
        }
        return null
    }
}
