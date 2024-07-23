package github.zimo.autojsx.server

import com.intellij.ide.BrowserUtil
import github.zimo.autojsx.pojo.ApplicationListPojo
import github.zimo.autojsx.pojo.RunningListPojo
import github.zimo.autojsx.util.base64_image
import github.zimo.autojsx.util.logE
import github.zimo.autojsx.util.logI
import github.zimo.autojsx.util.resourceAsStream
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


object VertxStart {
    // TODO VertxCommandServer 字段与 start 等 启动结束方法迁移到该类
}


object VertxCommandServer {
    var vertx: Vertx? = null
    val devicesWs: HashMap<String, ServerWebSocket> = HashMap()
    val selectDevicesWs: HashMap<String, ServerWebSocket> = HashMap()
    var isStart = false
    var ipAddress = "Unknown"
    var port = 9317

    /**
     * MainVerticle 接收并解析信息后将它放入到该 map 中，VertxServer 中指令方法将循环监听其变化直到超时
     */
    val content: HashMap<String, JsonObject> = HashMap()

    fun start(port: Int = -1): Future<String>? {
        if (vertx != null) return null
        if (port != -1) this.port = port
        vertx = Vertx.vertx()
        return vertx!!.deployVerticle(MainVerticle(this.port))
    }

    fun stop(): Future<Void>? {
        val close = vertx?.close()?.onComplete {
            if (it.succeeded()) {
                logI("服务器已停止")
            } else {
                logE("服务器无法被终止", it.cause())
            }
        }
        selectDevicesWs.clear()
        devicesWs.clear()
        isStart = false
        vertx = null
        return close
    }

    fun isActivity(): Boolean = vertx != null

    fun devicesEmpty(devices: HashMap<String, ServerWebSocket> = selectDevicesWs) {
        if (devices.isEmpty()) {
            ConsoleOutputV2.systemPrint("操作警告/W: 未选择任何设备,无法执行脚本")
        }
    }

    fun getServerIpAddress(): String {
        if (ipAddress != "Unknown") return ipAddress
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()

            // 检查网络接口是否启用并且不为虚拟网卡
            if (networkInterface.isUp && !networkInterface.isLoopback && !isVirtualNetworkInterface(networkInterface)) {
                val interfaceAddresses = networkInterface.interfaceAddresses
                for (interfaceAddress in interfaceAddresses) {
                    val address = interfaceAddress.address
                    // 检查是否为IPv4地址且不是回环地址，并且最后一位不是1
                    if (address is InetAddress
                        && !address.isLoopbackAddress
                        && address.hostAddress.contains(":").not()
                        && !isGatewayAddress(address.hostAddress)
                    ) {
                        ipAddress = address.hostAddress
                        return ipAddress
                    }
                }
            }
        }
        return "Unknown"
    }

    // 判断是否为虚拟网卡
    fun isVirtualNetworkInterface(networkInterface: NetworkInterface): Boolean {
        val name = networkInterface.name.lowercase()
        val virtualNetworkPrefixes = listOf("vmware", "vmnet", "vbox", "virbr", "docker", "dummy")
        return virtualNetworkPrefixes.any { name.startsWith(it) }
    }

    // 判断是否为常见的网关地址
    fun isGatewayAddress(ipAddress: String): Boolean {
        val parts = ipAddress.split(".")
        if (parts.size == 4) {
            return parts[3] == "1"
        }
        return false
    }

    object Command {
        /**
         * 关闭运行的脚本，只能关闭远程脚本
         */
        fun stop(jsPath: String, devices: HashMap<String, ServerWebSocket> = VertxCommandServer.selectDevicesWs) {
            devicesEmpty(devices)
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
        fun stopAll(devices: HashMap<String, ServerWebSocket> = VertxCommandServer.selectDevicesWs) {
            devicesEmpty(devices)
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
        fun runProject(path: String, devices: HashMap<String, ServerWebSocket> = VertxCommandServer.selectDevicesWs) {
            devicesEmpty(devices)
            val zipFile = File(path)
            val canonicalPath = zipFile.canonicalPath
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
                        logE("发送文件失败: $zipFile")
                    }
                }.onFailure {
                    logE("读取文件失败: exists: ${zipFile.exists()}", it)
                }
            }
        }

        /**
         * 发送项目
         * @param path 本地 zip 文件路径
         */
        fun saveProject(path: String, devices: HashMap<String, ServerWebSocket> = VertxCommandServer.selectDevicesWs) {
            devicesEmpty(devices)
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
                        logE("发送文件失败: $path")
                    }
                }.onFailure {
                    logE("读取文件失败: exists: ${File(path).exists()}", it)
                }
            }
        }

        /**
         * 发送项目到脚本根路径
         * @param path 本地 zip 文件路径
         */
        fun saveProjectToRoot(
            path: String,
            devices: HashMap<String, ServerWebSocket> = VertxCommandServer.selectDevicesWs
        ) {
            devicesEmpty(devices)
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
                        logE("发送文件失败: $path")
                    }
                }.onFailure {
                    logE("读取文件失败: exists: ${File(path).exists()}", it)
                }
            }
        }


        /**
         * 保存文件
         * @param path 本地文本文件
         */
        fun saveJS(path: String, devices: HashMap<String, ServerWebSocket> = VertxCommandServer.selectDevicesWs) {
            devicesEmpty(devices)
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
                    logE("发送文件失败: $path")
                }

            }
        }

        /**
         * 运行js文件
         * @param path 本地文本文件
         */
        fun runJS(path: String, devices: HashMap<String, ServerWebSocket> = VertxCommandServer.selectDevicesWs) {
            devicesEmpty(devices)
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
                    logE("发送文件失败: $path")
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
            devices: HashMap<String, ServerWebSocket> = VertxCommandServer.selectDevicesWs,
        ) {
            devicesEmpty(devices)
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
        fun rerunJS(path: String, devices: HashMap<String, ServerWebSocket> = VertxCommandServer.selectDevicesWs) {
            devicesEmpty(devices)
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
                    logE("发送文件失败: $path")
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
            devices: HashMap<String, ServerWebSocket> = VertxCommandServer.selectDevicesWs,
        ) {
            devicesEmpty(devices)
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
        fun getRunningList(
            callback: (array: List<RunningListPojo>) -> Unit,
            devices: HashMap<String, ServerWebSocket> = VertxCommandServer.selectDevicesWs,
        ) {
            content["getRunningList"] = JsonObject("{\"ID\": \"0\"}")
            devicesEmpty(devices)
            var readBytes: ByteArray = ByteArray(0)
            resourceAsStream("script/RunningScripts.js")?.apply {
                readBytes = readBytes()
                close()
            }
            if (readBytes.isEmpty()) return
            val uuid = UUID.randomUUID().toString()
            val script =
                "let values = ['http://${getServerIpAddress()}:$port/receive','${uuid}'];\r\n" + String(readBytes)
            runJsByString("getRunningList", script, devices)
            val startTime = System.currentTimeMillis()
            while ((System.currentTimeMillis() - startTime) < 3000) {
                content["getRunningList"]?.let {
                    if (uuid == it.getString("ID")) {
                        val array: ArrayList<RunningListPojo> = ArrayList()
                        it.getJsonArray("value").forEach {
                            val pojo = RunningListPojo()
                            JsonObject(it.toString()).apply {
                                pojo.engineId = getInteger("engineId")
                                pojo.engineScriptArgv = getString("engineScriptArgv")
                                pojo.isStopped = getBoolean("isStopped")
                                pojo.engineScriptCwd = getString("engineScriptCwd")
                                pojo.source = getString("source")
                                pojo.sourceName = getString("sourceName")
                            }
                            if (!pojo.sourceName.contains("getRunningList")) array.add(pojo)
                        }
                        callback(array)
                        return
                    }
                }
            }
            if (!content.contains("getRunningList")) {
                logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 message: getRunningList。诊断为回传数据超时")
            } else if (content["getRunningList"]?.getString("ID") == uuid) {
                logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 uuid: $uuid 。请重试")
            }
            callback(ArrayList<RunningListPojo>())
        }

        /**
         * 通过ID停止一个脚本的运行。 注意： 该方法不能完全强制停止被阻塞的脚本
         *
         */
        fun stopScriptByID(id: Int, devices: HashMap<String, ServerWebSocket> = VertxCommandServer.selectDevicesWs) {
            devicesEmpty(devices)
            var readBytes: ByteArray = ByteArray(0)
            resourceAsStream("script/StopScriptByID.js")?.apply {
                readBytes = readBytes()
                close()
            }
            if (readBytes.isEmpty()) return
            val script =
                "let values = [$id];\r\n" + String(readBytes)
            runJsByString("getRunningList", script, devices)
        }

        /**
         * 通过文件名称停止一个脚本的运行
         */
        fun stopScriptBySourceName(
            name: String,
            devices: HashMap<String, ServerWebSocket> = VertxCommandServer.selectDevicesWs,
        ) {
            devicesEmpty(devices)
            var readBytes: ByteArray = ByteArray(0)
            resourceAsStream("script/StopScriptByName.js")?.apply {
                readBytes = readBytes()
                close()
            }
            if (readBytes.isEmpty()) return
            val script =
                "let values = [$name];\r\n" + String(readBytes)
            runJsByString("getRunningList", script, devices)
        }

        /**
         * 获取当前屏幕上的所有节点并记录成xml
         */
        fun getNodes(
            callback: (xml: String) -> Unit,
            devices: HashMap<String, ServerWebSocket> = VertxCommandServer.selectDevicesWs,
        ) {
            content["getNodes"] = JsonObject("{\"ID\": \"0\"}")
            devicesEmpty(devices)
            var readBytes: ByteArray = ByteArray(0)
            resourceAsStream("script/NodesXML.js")?.apply {
                readBytes = readBytes()
                close()
            }
            if (readBytes.isEmpty()) return
            val uuid = UUID.randomUUID().toString()
            val script =
                "let values = ['http://${getServerIpAddress()}:$port/receive','${uuid}'];\r\n" + String(readBytes)
            runJsByString("getNodes", script, devices)
            val startTime = System.currentTimeMillis()
            while ((System.currentTimeMillis() - startTime) < 3000) {
                content["getNodes"]?.let {
                    if (uuid == it.getString("ID")) {
                        callback(it.getString("value"))
                        return
                    }
                }
            }
            if (!content.contains("getNodes")) {
                logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 message: getNodes。诊断为回传数据超时")
            } else if (content["getNodes"]?.getString("ID") == uuid) {
                logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 uuid: $uuid 。请重试")
            }
            callback("")
        }

        /**
         * 获取设备上的应用信息列表
         */
        fun getApplications(
            callback: (list: ArrayList<ApplicationListPojo>) -> Unit,
            devices: HashMap<String, ServerWebSocket> = VertxCommandServer.selectDevicesWs,
        ) {
            content["getApplications"] = JsonObject("{\"ID\": \"0\"}")
            devicesEmpty(devices)
            var readBytes: ByteArray = ByteArray(0)
            resourceAsStream("script/ApplicationList.js")?.apply {
                readBytes = readBytes()
                close()
            }
            if (readBytes.isEmpty()) return
            val uuid = UUID.randomUUID().toString()
            val script =
                "let values = ['http://${getServerIpAddress()}:$port/receive','${uuid}'];\r\n" + String(readBytes)
            runJsByString("getApplications", script, devices)
            val startTime = System.currentTimeMillis()
            while ((System.currentTimeMillis() - startTime) < 8 * 1000) {
                content["getApplications"]?.let {
                    if (uuid == it.getString("ID")) {
                        val list = ArrayList<ApplicationListPojo>()
                        it.getJsonArray("value").forEach {
                            val pojo = ApplicationListPojo()
                            JsonObject(it.toString()).apply {
                                pojo.name = getString("name")
                                pojo.packageName = getString("packageName")
                                pojo.installTime = getLong("installTime")
                                pojo.versionName = getString("versionName")
                                pojo.versionCode = getInteger("versionCode")
                                pojo.icon = getString("icon")?.replace("\n", "")?.replace(" ", "")
                                runCatching {
                                    pojo.iconImage = pojo.icon?.let { it1 -> base64_image(it1) }
                                }.onFailure {
                                    logE("Failed to get application ", it)
                                }
                            }
                            list.add(pojo)
                        }
                        callback(list)
                        return
                    }
                }
            }
            if (!content.contains("getApplications")) {
                logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 message: getApplications。诊断为回传数据超时")
            } else if (content["getApplications"]?.getString("ID") == uuid) {
                logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 uuid: $uuid 。请重试")
            }

            callback(ArrayList())
        }

        /**
         * 截图
         */
        fun getScreenshot(
            callback: (base64: String) -> Unit,
            devices: HashMap<String, ServerWebSocket> = VertxCommandServer.selectDevicesWs,
        ) {
            content["getScreenshot"] = JsonObject("{\"ID\": \"0\"}")
            devicesEmpty(devices)
            var readBytes: ByteArray = ByteArray(0)
            resourceAsStream("script/Screenshot.js")?.apply {
                readBytes = readBytes()
                close()
            }
            if (readBytes.isEmpty()) return
            val uuid = UUID.randomUUID().toString()
            val script =
                "let values = ['http://${getServerIpAddress()}:$port/receive','${uuid}'];\r\n" + String(readBytes)
            runJsByString("getScreenshot", script, devices)
            val startTime = System.currentTimeMillis()
            while ((System.currentTimeMillis() - startTime) < 6000) {
                content["getScreenshot"]?.let {
                    if (uuid == it.getString("ID")) {
                        callback(it.getString("value"))
                        return
                    }
                }
            }
            if (!content.contains("getScreenshot")) {
                logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 message: getScreenshot。诊断为回传数据超时")
            } else if (content["getScreenshot"]?.getString("ID") == uuid) {
                logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 uuid: $uuid 。请重试")
            }
            callback("")
        }

        @Deprecated("混淆不在使用脚本回传")
        var isStartConfusion = false

        /**
         * 截图
         */
        @Deprecated("混淆不在使用脚本回传")
        fun confusion(
            src: File,
            out: File,
        ): Boolean {
            if (isStartConfusion) {
                logE("混淆正在进行，请等待超时结束")
                isStartConfusion = false
                return false
            }
            isStartConfusion = true

            if (!(src.exists() && src.isDirectory && out.exists() && out.isDirectory)) {
                logE("目录未存在: src: [${src}]  out: [${out}]")
                isStartConfusion = false
                return false
            }

            val cacheJSON = JsonObject()
            val cacheArray = JsonArray()
            src.listFiles()?.forEach {
                if (!it.name.endsWith(".js")) return@forEach
                val valJSON = JsonObject()
                valJSON.put(it.name, it.readText())
                cacheArray.add(valJSON)
            }
            cacheJSON.put("text", cacheArray)
            CacheTexts.cacheOutJson = cacheJSON.toString()
            BrowserUtil.browse("http://${VertxCommandServer.getServerIpAddress()}:${VertxCommandServer.port}/confusion.html")

            val startTime = System.currentTimeMillis()
            while ((System.currentTimeMillis() - startTime) < 60 * 1000 && isStartConfusion) {
                content["confusion"]?.let {
                    it.getJsonArray("text").forEach {
                        JsonObject(it.toString()).forEach {
                            out.resolve("/${it.key}").writeText(it.value.toString())
                        }
                    }

                    content.remove("confusion")
                    isStartConfusion = false
                    return true
                }
            }
            isStartConfusion = false
            content.remove("confusion")
            logE("在规定时间内未能等到客户端回传混淆文件")
            return false
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