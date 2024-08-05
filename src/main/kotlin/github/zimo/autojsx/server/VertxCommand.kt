package github.zimo.autojsx.server

import github.zimo.autojsx.pojo.ApplicationListPojo
import github.zimo.autojsx.pojo.RunningListPojo
import github.zimo.autojsx.server.AutojsJsonBuilder.Companion.rerunJsFileJson
import github.zimo.autojsx.server.AutojsJsonBuilder.Companion.runProjectJson
import github.zimo.autojsx.server.AutojsJsonBuilder.Companion.saveJsFileJson
import github.zimo.autojsx.server.AutojsJsonBuilder.Companion.saveProjectJson
import github.zimo.autojsx.uiHierarchyAnalysis.UIHierarchy
import github.zimo.autojsx.util.base64_image
import github.zimo.autojsx.util.logE
import github.zimo.autojsx.util.resourceAsStream
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

object VertxCommand {
    /**
     * 关闭运行的脚本，只能关闭远程脚本
     */
    fun stop(jsPath: String, devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs) {
        VertxServer.devicesEmpty(devices)
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
    fun stopAll(devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs) {
        VertxServer.devicesEmpty(devices)
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
     * 运行项目
     * @param path 本地 zip 文件路径
     */
    fun runProject(path: String, devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs) {
        VertxServer.vertx!!.fileSystem().readFile(path).onSuccess {
            runProject(it.bytes, File(path).canonicalPath, devices)
        }.onFailure {
            logE("读取文件失败: $path", it)
        }
    }

    /**
     * 运行项目
     * @param bytes  zip bytes
     */
    fun runProject(
        bytes: ByteArray,
        filePathName: String,
        devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs
    ) {
        VertxServer.devicesEmpty(devices)
        devices.forEach { (key, ws) ->
            if (ws.isClosed) return@forEach
            ws.writeBinaryMessage(Buffer.buffer().appendBytes(bytes)).onSuccess {
                ws.writeTextMessage(runProjectJson(bytes.md5(), filePathName).toString())
            }.onFailure {
                logE("发送文件失败")
            }
        }
    }

    /**
     * 发送项目
     * @param path 本地 zip 文件路径
     */
    fun saveProject(path: String, devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs) {
        VertxServer.vertx!!.fileSystem().readFile(path).onSuccess { fileBuffer ->
            saveProject(fileBuffer.bytes, path, devices)
        }.onFailure {
            logE("读取文件失败: exists: ${File(path).exists()}", it)
        }
    }

    /**
     * 发送项目到脚本根路径
     * @param path 本地 zip 文件路径
     */
    fun saveProjectToRoot(
        path: String,
        devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs
    ) {
        VertxServer.vertx!!.fileSystem().readFile(path).onSuccess { fileBuffer ->
            saveProject(fileBuffer.bytes, "/", devices)
        }.onFailure {
            logE("读取文件失败: exists: ${File(path).exists()}", it)
        }
    }

    /**
     * 发送项目
     * @param bytes zip bytes
     */
    fun saveProject(
        bytes: ByteArray,
        filePathName: String,
        devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs
    ) {
        VertxServer.devicesEmpty(devices)
        devices.forEach { (key, ws) ->
            if (ws.isClosed) return@forEach
            ws.writeBinaryMessage(Buffer.buffer().appendBytes(bytes)).onSuccess {
                val returnData = saveProjectJson(bytes.md5(), filePathName)
                ws.writeTextMessage(returnData.toString())
            }.onFailure {
                logE("发送文件失败: $filePathName", it)
            }
        }
    }


    /**
     * 保存文件
     * @param path 本地文本文件
     */
    fun saveJS(path: String, devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs) {
        VertxServer.devicesEmpty(devices)
        val canonicalPath = File(path).canonicalPath
        devices.forEach { (key, ws) ->
            if (ws.isClosed) return@forEach
            VertxServer.vertx!!.fileSystem().readFile(canonicalPath).onSuccess {
                ws.writeTextMessage(saveJsFileJson(canonicalPath, it.toString(Charset.forName("UTF-8"))).toString())
            }.onFailure {
                logE("发送文件失败: $canonicalPath", it)
            }
        }
    }

    /**
     * 运行js文件
     * @param path 本地文本文件
     */
    fun runJS(path: String, devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs) {
        VertxServer.devicesEmpty(devices)
        val canonicalPath = File(path).canonicalPath
        VertxServer.vertx!!.fileSystem().readFile(canonicalPath).onSuccess {
            runJs(canonicalPath, it.toString(Charset.forName("UTF-8")), devices)
        }.onFailure {
            logE("发送文件失败: $path", it)
        }
    }

    /**
     * 运行js脚本
     * @param name 脚本名称
     * @param content 脚本内容
     */
    fun runJs(
        name: String = "main.js",
        content: String,
        devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs,
    ) {
        VertxServer.devicesEmpty(devices)
        val canonicalPath = File(name).canonicalPath
        devices.forEach { (key, ws) ->
            if (ws.isClosed) return@forEach
//            ws.writeTextMessage(runJsFileJson(canonicalPath, content).toString())
            // 以重新运行脚本的方式运行脚本，防止脚本被多次启动
            ws.writeTextMessage(rerunJsFileJson(canonicalPath, content).toString())
        }
    }

    /**
     * 重新运行js文件
     * @param path 本地文本文件
     */
    fun rerunJS(path: String, devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs) {
        VertxServer.devicesEmpty(devices)
        val canonicalPath = File(path).canonicalPath
        VertxServer.vertx!!.fileSystem().readFile(canonicalPath).onSuccess {
            rerunJs(canonicalPath, it.toString(Charset.forName("UTF-8")), devices)
        }.onFailure {
            logE("发送文件失败: $path", it)
        }
    }

    /**
     * 重新运行js脚本
     * @param name 脚本名称
     * @param content 脚本内容
     */
    fun rerunJs(
        name: String = "main.js",
        content: String,
        devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs,
    ) {
        VertxServer.devicesEmpty(devices)
        devices.forEach { (key, ws) ->
            if (ws.isClosed) return@forEach
            ws.writeTextMessage(rerunJsFileJson(name, content).toString())
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取运行时脚本列表
     */
    fun getRunningList(
        callback: (array: List<RunningListPojo>) -> Unit,
        devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs,
    ) {
        VertxServer.content["getRunningList"] = JsonObject("{\"ID\": \"0\"}")
        VertxServer.devicesEmpty(devices)
        var readBytes: ByteArray = ByteArray(0)
        resourceAsStream("script/RunningScripts.js")?.apply {
            readBytes = readBytes()
            close()
        }
        if (readBytes.isEmpty()) return
        val uuid = UUID.randomUUID().toString()
        val script =
            "let values = ['http://${VertxServer.getServerIpAddress()}:${VertxServer.port}/receive','${uuid}'];\r\n" + String(
                readBytes
            )
        runJs("[SystemScript]GetTheRunningScript", script, devices)
        val startTime = System.currentTimeMillis()
        while ((System.currentTimeMillis() - startTime) < 3000) {
            VertxServer.content["getRunningList"]?.let {
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
        if (!VertxServer.content.contains("getRunningList")) {
            logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 message: getRunningList。诊断为回传数据超时")
        } else if (VertxServer.content["getRunningList"]?.getString("ID") == uuid) {
            logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 uuid: $uuid 。请重试")
        }
        callback(ArrayList<RunningListPojo>())
    }

    /**
     * 通过ID停止一个脚本的运行。 注意： 该方法不能完全强制停止被阻塞的脚本
     *
     */
    fun stopScriptByID(id: Int, devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs) {
        VertxServer.devicesEmpty(devices)
        var readBytes: ByteArray = ByteArray(0)
        resourceAsStream("script/StopScriptByID.js")?.apply {
            readBytes = readBytes()
            close()
        }
        if (readBytes.isEmpty()) return
        val script =
            "let values = [$id];\r\n" + String(readBytes)
        runJs("StopScriptByID", script, devices)
    }

    /**
     * 通过文件名称停止一个脚本的运行
     */
    fun stopScriptBySourceName(
        name: String,
        devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs,
    ) {
        VertxServer.devicesEmpty(devices)
        var readBytes: ByteArray = ByteArray(0)
        resourceAsStream("script/StopScriptByName.js")?.use {
            readBytes = it.readBytes()
        }
        if (readBytes.isEmpty()) return
        val script =
            "let values = ['$name'];\r\n" + String(readBytes)
        runJs("StopScriptByName", script, devices)
    }

    /**
     * 获取当前屏幕上的所有节点并记录成xml
     */
    fun getNodesAsXml(
        callback: (xml: String) -> Unit,
        devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs,
    ) {
        VertxServer.content["getNodes"] = JsonObject("{\"ID\": \"0\"}")
        VertxServer.devicesEmpty(devices)
        var readBytes: ByteArray = ByteArray(0)
        resourceAsStream("script/NodesXML.js")?.apply {
            readBytes = readBytes()
            close()
        }
        if (readBytes.isEmpty()) return
        val uuid = UUID.randomUUID().toString()
        val script =
            "let values = ['http://${VertxServer.getServerIpAddress()}:${VertxServer.port}/receive','${uuid}'];\r\n" + String(
                readBytes
            )
        runJs("getNodes", script, devices)
        val startTime = System.currentTimeMillis()
        while ((System.currentTimeMillis() - startTime) < 3000) {
            VertxServer.content["getNodes"]?.let {
                if (uuid == it.getString("ID")) {
                    callback(it.getString("value"))
                    return
                }
            }
        }
        if (!VertxServer.content.contains("getNodes")) {
            logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 message: getNodes。诊断为回传数据超时")
        } else if (VertxServer.content["getNodes"]?.getString("ID") == uuid) {
            logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 uuid: $uuid 。请重试")
        }
        callback("")
    }

    /**
     * 获取当前屏幕上的所有节点并记录成 json
     */
    fun getNodesAsJson(
        callback: (json: UIHierarchy?) -> Unit,
        devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs,
    ) {
        VertxServer.content["getNodesJson"] = JsonObject("{\"ID\": \"0\"}")
        VertxServer.devicesEmpty(devices)
        var readBytes: ByteArray = ByteArray(0)
        resourceAsStream("script/NodesJson.js")?.apply {
            readBytes = readBytes()
            close()
        }
        if (readBytes.isEmpty()) return
        val uuid = UUID.randomUUID().toString()
        val script =
            "let values = ['http://${VertxServer.getServerIpAddress()}:${VertxServer.port}/receive','${uuid}'];\r\n" + String(
                readBytes
            )
        runJs("getNodesJson", script, devices)
        val startTime = System.currentTimeMillis()
        while ((System.currentTimeMillis() - startTime) < 3000) {
            VertxServer.content["getNodesJson"]?.let {
                if (uuid == it.getString("ID")) {
                    callback(UIHierarchy.parse(it.getString("value")))
                    return
                }
            }
        }
        if (!VertxServer.content.contains("getNodesJson")) {
            logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 message: getNodesJson。诊断为回传数据超时")
        } else if (VertxServer.content["getNodes"]?.getString("ID") == uuid) {
            logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 uuid: $uuid 。请重试")
        }
        callback(null)
    }

    /**
     * 获取设备上的应用信息列表
     */
    fun getApplications(
        callback: (list: ArrayList<ApplicationListPojo>) -> Unit,
        devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs,
    ) {
        VertxServer.content["getApplications"] = JsonObject("{\"ID\": \"0\"}")
        VertxServer.devicesEmpty(devices)
        var readBytes: ByteArray = ByteArray(0)
        resourceAsStream("script/ApplicationList.js")?.apply {
            readBytes = readBytes()
            close()
        }
        if (readBytes.isEmpty()) return
        val uuid = UUID.randomUUID().toString()
        val script =
            "let values = ['http://${VertxServer.getServerIpAddress()}:${VertxServer.port}/receive','${uuid}'];\r\n" + String(
                readBytes
            )
        runJs("getApplications", script, devices)
        val startTime = System.currentTimeMillis()
        while ((System.currentTimeMillis() - startTime) < 8 * 1000) {
            VertxServer.content["getApplications"]?.let {
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
        if (!VertxServer.content.contains("getApplications")) {
            logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 message: getApplications。诊断为回传数据超时")
        } else if (VertxServer.content["getApplications"]?.getString("ID") == uuid) {
            logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 uuid: $uuid 。请重试")
        }

        callback(ArrayList())
    }

    /**
     * 截图
     */
    fun getScreenshot(
        callback: (base64: String) -> Unit,
        devices: HashMap<String, ServerWebSocket> = VertxServer.selectDevicesWs,
    ) {
        VertxServer.content["getScreenshot"] = JsonObject("{\"ID\": \"0\"}")
        VertxServer.devicesEmpty(devices)
        var readBytes: ByteArray = ByteArray(0)
        resourceAsStream("script/Screenshot.js")?.apply {
            readBytes = readBytes()
            close()
        }
        if (readBytes.isEmpty()) return
        val uuid = UUID.randomUUID().toString()
        val script =
            "let values = ['http://${VertxServer.getServerIpAddress()}:${VertxServer.port}/receive','${uuid}'];\r\n" + String(
                readBytes
            )
        runJs("getScreenshot", script, devices)
        val startTime = System.currentTimeMillis()
        while ((System.currentTimeMillis() - startTime) < 6000) {
            VertxServer.content["getScreenshot"]?.let {
                if (uuid == it.getString("ID")) {
                    callback(it.getString("value"))
                    return
                }
            }
        }
        if (!VertxServer.content.contains("getScreenshot")) {
            logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 message: getScreenshot。诊断为回传数据超时")
        } else if (VertxServer.content["getScreenshot"]?.getString("ID") == uuid) {
            logE("未能获取到Autojs.WIFI 内部脚本执行结果，未能查找到 uuid: $uuid 。请重试")
        }
        callback("")
    }


    fun ByteArray.md5(): String {
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