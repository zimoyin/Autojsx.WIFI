package github.zimo.autojsx.server

import com.intellij.openapi.project.ProjectManager
import github.zimo.autojsx.server.VertxServer.devicesWs
import github.zimo.autojsx.server.VertxServer.selectDevicesWs
import github.zimo.autojsx.util.logE
import github.zimo.autojsx.util.logI
import github.zimo.autojsx.util.logW
import github.zimo.autojsx.util.zipBytes
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler
import java.io.File
import java.io.FileNotFoundException
import java.util.*


class MainVerticle(val port: Int = 9317) : AbstractVerticle() {


    override fun start(startPromise: Promise<Void>) {
        val router = Router.router(vertx)
        vertx
            .createHttpServer()
            .requestHandler(router)
            .webSocketHandler { ws ->
                var device = "device"
                val delay: Long = 15 * 1000
                var isTimeout: Boolean = false
                // 定时器 ID，用于取消定时器
                val timerId = vertx.setPeriodic(delay) { timerId: Long? ->
                    // 检查 WebSocket 活动情况
                    if (ws.isClosed) {
                        // WebSocket 已经关闭，或者已经有消息到达，取消定时器
                        vertx.cancelTimer(timerId!!)
                    } else {
                        // WebSocket 超时，手动关闭连接
                        logW("WebSocket connection timed out")
                        isTimeout = true
                        ws.close()
                    }
                }

                ws.textMessageHandler {
                    val jsonObject = it.asJsonObject()
                    val returnData = JsonObject()
                    when (jsonObject.getString("type")) {
                        "hello" -> {
                            //info
                            val app_version = jsonObject.getJsonObject("data").getString("app_version")
                            val app_version_code = jsonObject.getJsonObject("data").getString("app_version_code")
                            val client_version = jsonObject.getJsonObject("data").getString("client_version")
                            device = jsonObject.getJsonObject("data").getString("device_name")
                            val then19 = app_version_code.replace(".", "").toInt() >= 629
                            //return data
                            returnData.put("message_id", UUID.randomUUID().toString())
                            if (then19) returnData.put("data", "ok")
                            else returnData.put("data", "连接成功")
                            if (then19) returnData.put("version", "1.109.0")
                            returnData.put("debug", false)
                            returnData.put("type", "hello")
                            //新设备介入
                            selectDevicesWs[device] = ws
                            devicesWs[device] = ws
                            Devices.add(device)
                            ConsoleOutput.systemPrint("新设备接入/I： Device $device  AppVersion: $app_version ClientVersion: $client_version")
                        }

                        "ping" -> {
                            // 收到消息，重置定时器
                            vertx.cancelTimer(timerId);
                            returnData.put("data", jsonObject.getLong("data"))
                            returnData.put("type", "pong")
                        }

                        "log" -> {
                            var text = jsonObject.getJsonObject("data").getString("log")
                            if (text.contains(" ------------ ") && text.contains("运行结束，用时")) {
                                text = text.replace("\n", " ")
                                    .replace("  "," ")
                                    .replace(" ------------ ", "") + "\r\n"
                            }
                            selectDevicesWs.forEach { (key, value) ->
                                if (value == ws) ConsoleOutput.println(device, text)
                            }
                        }
                    }
                    if (!returnData.isEmpty) ws.writeTextMessage(returnData.toString())
                }

                ws.closeHandler {
                    //remove device
                    selectDevicesWs.remove("device")
                    devicesWs.remove("device")

//                    println("WebSocket connection closed")
                    Devices.remove(device)
                    if (isTimeout) logI("设备超时离线： Device $device")
                    else logI("设备主动离线： Device $device")
                }
            }
            .listen(port) { http ->
                if (http.succeeded()) {
                    startPromise.complete()
                    logI("Autojsx 服务器在 ${VertxServer.getServerIpAddress()}:${VertxServer.port} 启动")
                    VertxServer.isStart = true
                } else {
                    startPromise.fail(http.cause())
                    logE("服务器无法启动在端口$port", http.cause())
                    VertxServer.stop()
                }
            }

        router.route("/receive").handler(BodyHandler.create()).handler { context ->
            kotlin.runCatching {
                val jsonObject = context.body().asJsonObject()
                VertxServer.content[jsonObject.getString("message")] = jsonObject
                context.response().end("ok")
            }.onFailure {
                logE("receive 接收数据失败", it)
                context.response().end("error")
            }
        }

        router.route("/upload_path").handler(BodyHandler.create()).handler { context ->
            kotlin.runCatching {
                val path = context.body().asString()
                logI("upload_path $path")

                val file = File(path).apply {
                    if (!exists()) throw FileNotFoundException("文件不存在")
                }

                if (file.isFile) {
                    VertxCommand.saveJS(path)
                } else {
                    val name = file.listFiles()?.first { it.name == "project.json" && it.isFile }.let {
                        runCatching { JsonObject(it?.readText()).getString("name") }.getOrElse { path }
                    }
                    VertxCommand.saveProject(zipBytes(path), name)
                }

                context.response().end("ok")
            }.onFailure {
                logE("upload_path 接收数据失败", it)
                context.response().apply {
                    statusCode = 500
                    end("error")
                }
            }
        }

        router.route("/upload_resources_path").handler(BodyHandler.create()).handler { context ->
            kotlin.runCatching {
                val path = context.body().asString()
                logI("upload_resources_path $path")

                val file = File(path).apply {
                    if (!exists()) throw FileNotFoundException("文件不存在")
                }

                if (file.isFile) {
                    logE("upload_resources_path 不支持上传文件")
                    return@handler
                } else {
                    val name = ProjectManager.getInstance().openProjects.firstOrNull()?.name?.let { "$it/" }?:""
                    VertxCommand.saveProject(zipBytes(path), "$name/$path")
                }

                context.response().end("ok")
            }.onFailure {
                logE("upload_resources_path 接收数据失败", it)
                context.response().apply {
                    statusCode = 500
                    end("error")
                }
            }
        }

        router.route("/upload_run_path").handler(BodyHandler.create()).handler { context ->
            kotlin.runCatching {
                val path = context.body().asString()
                logI("upload_run_path: $path")

                val file = File(path).apply {
                    if (!exists()) throw FileNotFoundException("文件不存在")
                }

                if (file.isFile) {
                    VertxCommand.saveJS(path)
                } else {
                    val name = file.listFiles()?.first { it.name == "project.json" && it.isFile }.let {
                        runCatching { JsonObject(it?.readText()).getString("name") }.getOrElse { path }
                    }
                    VertxCommand.runProject(zipBytes(path), name)
                }

                context.response().end("ok")
            }.onFailure {
                logE("upload_run_path 接收数据失败", it)
                context.response().apply {
                    statusCode = 500
                    end("error")
                }
            }
        }
        router.route("/*").handler(StaticHandler.create())
    }

    fun String.asJsonObject(): JsonObject = JsonObject(this)
}
