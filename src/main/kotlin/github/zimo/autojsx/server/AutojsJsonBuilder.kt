package github.zimo.autojsx.server

import io.vertx.core.json.JsonObject
import java.util.*

/**
 *
 * @author : zimo
 * @date : 2024/08/01
 */
class AutojsJsonBuilder {

    companion object {
        /**
         * 运行JS文件JSON
         */
        fun runJsFileJson(fileName: String, script: String): JsonObject {
            return projectJson(null, "run", fileName, script)
        }

        /**
         * 重新运行JS文件JSON
         */
        fun rerunJsFileJson(fileName: String, script: String): JsonObject {
            return projectJson(null, "rerun", fileName, script)
        }

        /**
         * 保存远端JS文件JSON
         */
        fun saveJsFileJson(fileName: String, script: String): JsonObject {
            return projectJson(null, "save", fileName, script)
        }


        /**
         * 运行远端项目JSON
         */
        fun runProjectJson(md5: String, projectName: String): JsonObject {
            return projectJson(md5, "run_project", projectName)
        }

        /**
         * 保存项目 JSON 数据
         */
        fun saveProjectJson(md5: String, projectName: String): JsonObject {
            return projectJson(md5, "save_project", projectName)
        }

        /**
         * @param name 文件保存到设置中的位置，通常是项目名称
         */
        fun projectJson(md5: String?, command: String, name: String? = null, script: String? = null): JsonObject {
            val mid = UUID.randomUUID().toString()
            return JsonObject().apply {
                put("type", if (script != null) "command" else "bytes_command")
                put("message_id", mid)
                md5?.let { put("md5", it) }
                put("data", JsonObject().apply {
                    put("command", command)
                    put("id", mid)
                    put("name", name ?: if (script != null) "debug.js" else "debug")
                    script?.let { put("script", script) }
                })
            }
        }
    }
}