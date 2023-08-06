package github.zimo.autojsx.server

import github.zimo.autojsx.util.resourceAsStream
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.io.File
import java.io.IOException
import java.net.NetworkInterface
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

object VertxServer {
    private var vertx: Vertx? = null
    val devices: HashMap<String, ServerWebSocket> = HashMap()
    var isStart = false
    var port = -1
    val content: HashMap<String, JsonObject> = HashMap()

    fun start(port: Int = 9317): Future<String>? {
        if (vertx != null) return null
        this.port = port
        vertx = Vertx.vertx()
        return vertx!!.deployVerticle(MainVerticle(port))
    }

    fun stop(): Future<Void>? {
        val close = vertx?.close()
        this.port = -1
        vertx = null
        return close
    }

    fun isActivity(): Boolean = vertx != null

    fun getServerIpAddress(): String {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()
            val interfaceAddresses = networkInterface.interfaceAddresses
            for (interfaceAddress in interfaceAddresses) {
                val address = interfaceAddress.address
                if (!address.isLoopbackAddress && address.hostAddress.contains(":").not()) {
                    return address.hostAddress
                }
            }
        }
        return "Unknown"
    }

    object Command {
        /**
         * 关闭运行的脚本，只能关闭远程脚本
         * TODO 待验证
         */
        fun stop(jsPath: String, devices: HashMap<String, ServerWebSocket> = VertxServer.devices) {
            val canonicalPath = File(jsPath).canonicalPath
            devices.forEach { (key, ws) ->
                if (ws.isClosed) return@forEach

                val returnData = JsonObject()
                val data = JsonObject()
                returnData.put("type", "command")
                returnData.put("message_id", UUID.randomUUID().toString())
                returnData.put("data", data)
                data.put("command", "stop")
                data.put("id", canonicalPath)
                ws.writeTextMessage(returnData.toString())
            }
        }

        /**
         * 关闭运行的脚本，只能关闭远程脚本
         */
        fun stopAll(devices: HashMap<String, ServerWebSocket> = VertxServer.devices) {
            devices.forEach { (key, ws) ->
                if (ws.isClosed) return@forEach

                val returnData = JsonObject()
                val data = JsonObject()
                returnData.put("type", "command")
                returnData.put("message_id", UUID.randomUUID().toString())
                returnData.put("data", data)
                data.put("command", "stopAll")
                ws.writeTextMessage(returnData.toString())
            }
        }


        /**
         * 运行远端项目
         * @param path 本地 zip 文件路径
         */
        fun runProject(path: String, devices: HashMap<String, ServerWebSocket> = VertxServer.devices) {
            val canonicalPath = File(path).canonicalPath
            devices.forEach { (key, ws) ->
                if (ws.isClosed) return@forEach
                vertx!!.fileSystem().readFile(path).onSuccess {
                    val md5 = it.bytes.md5()
                    ws.writeBinaryMessage(it).onSuccess {
                        val returnData = JsonObject()
                        val data = JsonObject()
                        returnData.put("type", "bytes_command")
                        returnData.put("message_id", UUID.randomUUID().toString())
                        returnData.put("md5", md5)
                        returnData.put("data", data)
                        data.put("command", "run_project")
                        data.put("id", canonicalPath)
                        data.put("name", canonicalPath)
                        ws.writeTextMessage(returnData.toString())
                    }.onFailure {
                        println("发送文件失败")
                    }
                }.onFailure {
                    println("读取文件失败")
                    it.printStackTrace()
                }
            }
        }

        /**
         * 发送项目
         * @param path 本地 zip 文件路径
         */
        fun saveProject(path: String, devices: HashMap<String, ServerWebSocket> = VertxServer.devices) {
            val canonicalPath = File(path).canonicalPath
            devices.forEach { (key, ws) ->
                if (ws.isClosed) return@forEach
                vertx!!.fileSystem().readFile(path).onSuccess {
                    val bytes = it.bytes
                    ws.writeBinaryMessage(it).onSuccess {
                        val returnData = JsonObject()
                        val data = JsonObject()
                        returnData.put("type", "bytes_command")
                        returnData.put("message_id", UUID.randomUUID().toString())
                        returnData.put("md5", bytes.md5())
                        returnData.put("data", data)
                        data.put("command", "save_project")
                        data.put("id", canonicalPath)
                        data.put("name", canonicalPath)
                        ws.writeTextMessage(returnData.toString())
                    }.onFailure {
                        println("发送文件失败")
                    }
                }.onFailure {
                    println("读取文件失败")
                    it.printStackTrace()
                }
            }
        }

        /**
         * 发送项目到脚本根路径
         * @param path 本地 zip 文件路径
         */
        fun saveProjectToRoot(path: String, devices: HashMap<String, ServerWebSocket> = VertxServer.devices) {
            val canonicalPath = File(path).canonicalPath
            devices.forEach { (key, ws) ->
                if (ws.isClosed) return@forEach
                vertx!!.fileSystem().readFile(path).onSuccess {
                    val bytes = it.bytes
                    ws.writeBinaryMessage(it).onSuccess {
                        val returnData = JsonObject()
                        val data = JsonObject()
                        returnData.put("type", "bytes_command")
                        returnData.put("message_id", UUID.randomUUID().toString())
                        returnData.put("md5", bytes.md5())
                        returnData.put("data", data)
                        data.put("command", "save_project")
                        data.put("id", canonicalPath)
                        data.put("name", "/")
                        ws.writeTextMessage(returnData.toString())
                    }.onFailure {
                        println("发送文件失败")
                    }
                }.onFailure {
                    println("读取文件失败")
                    it.printStackTrace()
                }
            }
        }


        /**
         * 保存文件
         * @param path 本地文本文件
         */
        fun saveJS(path: String, devices: HashMap<String, ServerWebSocket> = VertxServer.devices) {
            val canonicalPath = File(path).canonicalPath
            devices.forEach { (key, ws) ->
                if (ws.isClosed) return@forEach
                vertx!!.fileSystem().readFile(path).onSuccess {
                    //  println("发送文件成功")
                    val returnData = JsonObject()
                    val data = JsonObject()
                    returnData.put("type", "command")
                    returnData.put("message_id", UUID.randomUUID().toString())
                    returnData.put("data", data)
                    data.put("command", "save")
                    data.put("id", canonicalPath)
                    data.put("name", canonicalPath)
                    data.put("script", it.toString(Charset.forName("UTF-8")))
                    ws.writeTextMessage(returnData.toString())
                }.onFailure {
                    println("读取文件失败")
                    it.printStackTrace()
                }
            }
        }

        /**
         * 运行js文件
         * @param path 本地文本文件
         */
        fun runJS(path: String, devices: HashMap<String, ServerWebSocket> = VertxServer.devices) {
            val canonicalPath = File(path).canonicalPath
            devices.forEach { (key, ws) ->
                if (ws.isClosed) return@forEach
                vertx!!.fileSystem().readFile(canonicalPath).onSuccess {
//                    val base64 = Base64.getEncoder().encodeToString(it.bytes)
                    //  println("发送文件成功")
                    val returnData = JsonObject()
                    val data = JsonObject()
                    returnData.put("type", "command")
                    returnData.put("message_id", UUID.randomUUID().toString())
                    returnData.put("data", data)
                    data.put("command", "run")
                    data.put("id", canonicalPath)
                    data.put("name", canonicalPath)
                    data.put("script", it.toString())
                    ws.writeTextMessage(returnData.toString())
                }.onFailure {
                    println("读取文件失败")
                    it.printStackTrace()
                }
            }
        }

        /**
         * 运行js脚本
         * @param name 脚本名称
         * @param content 脚本内容
         */
        fun runJsByString(
            name: String = "main.js",
            content: String,
            devices: HashMap<String, ServerWebSocket> = VertxServer.devices,
        ) {
            val canonicalPath = File(name).canonicalPath
            devices.forEach { (key, ws) ->
                if (ws.isClosed) return@forEach
                val returnData = JsonObject()
                val data = JsonObject()
                returnData.put("type", "command")
                returnData.put("message_id", UUID.randomUUID().toString())
                returnData.put("data", data)
                data.put("command", "run")
                data.put("id", canonicalPath)
                data.put("name", canonicalPath)
                data.put("script", content)
                ws.writeTextMessage(returnData.toString())
            }
        }

        /**
         * 重新运行js文件
         * @param path 本地文本文件
         */
        fun rerunJS(path: String, devices: HashMap<String, ServerWebSocket> = VertxServer.devices) {
            val canonicalPath = File(path).canonicalPath
            devices.forEach { (key, ws) ->
                if (ws.isClosed) return@forEach
                vertx!!.fileSystem().readFile(canonicalPath).onSuccess {
//                    val base64 = Base64.getEncoder().encodeToString(it.bytes)
                    //  println("发送文件成功")
                    val returnData = JsonObject()
                    val data = JsonObject()
                    returnData.put("type", "command")
                    returnData.put("message_id", UUID.randomUUID().toString())
                    returnData.put("data", data)
                    data.put("command", "rerun")
                    data.put("id", canonicalPath)
                    data.put("name", canonicalPath)
                    data.put("script", it.toString())
                    ws.writeTextMessage(returnData.toString())
                }.onFailure {
                    println("读取文件失败")
                    it.printStackTrace()
                }
            }
        }

        /**
         * 重新运行js脚本
         * @param name 脚本名称
         * @param content 脚本内容
         */
        fun rerunJsByString(
            name: String = "main.js",
            content: String,
            devices: HashMap<String, ServerWebSocket> = VertxServer.devices,
        ) {
            val canonicalPath = File(name).canonicalPath
            devices.forEach { (key, ws) ->
                if (ws.isClosed) return@forEach
                val returnData = JsonObject()
                val data = JsonObject()
                returnData.put("type", "command")
                returnData.put("message_id", UUID.randomUUID().toString())
                returnData.put("data", data)
                data.put("command", "run")
                data.put("id", canonicalPath)
                data.put("name", canonicalPath)
                data.put("script", content)
                ws.writeTextMessage(returnData.toString())
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * 获取运行时脚本列表
         */
        fun getRunningList(callback: (array: JsonArray?) -> Unit) {
            var readBytes: ByteArray = ByteArray(0)
            resourceAsStream("script/RunningScripts.js")?.apply {
                readBytes = readBytes()
                close()
            }
            if (readBytes.isEmpty()) return
            val uuid = UUID.randomUUID().toString()
            val script =
                "let values = ['http://${getServerIpAddress()}:$port','${uuid}'];\r\n" + String(readBytes)
            runJsByString("getRunningList", script)
            val startTime = System.currentTimeMillis()
            while ((System.currentTimeMillis() - startTime) < 3000) {
                content["getRunningList"]?.let {
                    if (uuid == it.getString("ID")) {
                        callback(it.getJsonArray("value"))
                    }
                }
            }
            callback(null)
        }


        private fun ByteArray.md5(): String {
            val sb = StringBuffer("")
            try {
                val md = MessageDigest.getInstance("MD5")
                md.update(this)
                val b = md.digest()
                var d: Int
                for (i in b.indices) {
                    d = b[i].toInt()
                    if (d < 0) {
                        d = b[i].toInt() and 0xff
                        // 与上一行效果等同
                        // i += 256;
                    }
                    if (d < 16) sb.append("0")
                    sb.append(Integer.toHexString(d))
                }
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return sb.toString()
        }
    }
}