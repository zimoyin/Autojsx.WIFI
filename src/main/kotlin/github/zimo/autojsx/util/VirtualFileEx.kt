package github.zimo.autojsx.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile


fun VirtualFile.findDirectory(name: String): VirtualFile? {
    if (this.isDirectory) {
        this.children.forEach {
            if (it.name == name) {
                return it
            }
        }
    }
    return null
}

fun VirtualFile.findOrCreateDirectory(name: String): VirtualFile {
    if (this.isDirectory) {
        this.children.forEach {
            if (it.name == name) {
                return it
            }
        }
    }
    return this.createChildDirectory(this, name)
}

val VirtualFile.isFile: Boolean
    get() = !this.isDirectory

fun VirtualFile.writeText(text: String) {
    val virtualFile = this
    ApplicationManager.getApplication().invokeLater {
        WriteCommandAction.runWriteCommandAction(null) {
            try {
                // Ensure the file is writable
                if (!virtualFile.isWritable) {
                    virtualFile.isWritable = true
                }

                // Check if file exists, if not create it
                if (!virtualFile.exists()) {
                    val parentDir = virtualFile.parent
                    if (parentDir != null && parentDir.isWritable) {
                        virtualFile.createChildData(this, virtualFile.name)
                    }
                }

                // Write text to file
                VfsUtil.saveText(virtualFile, text)

                // Refresh the file to ensure changes are reflected
                virtualFile.refresh(false, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}