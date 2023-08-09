package github.zimo.autojsx.server

import github.zimo.autojsx.server.VertxServer.devicesWs
import github.zimo.autojsx.server.VertxServer.selectDevicesWs
import github.zimo.autojsx.util.caseString
import github.zimo.autojsx.util.logE
import github.zimo.autojsx.util.logI
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
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
                // 定时器 ID，用于取消定时器
                val timerId = vertx.setPeriodic(delay) { timerId: Long? ->
                    // 检查 WebSocket 活动情况
                    if (ws.isClosed) {
                        // WebSocket 已经关闭，或者已经有消息到达，取消定时器
                        vertx.cancelTimer(timerId!!)
                    } else {
                        // WebSocket 超时，手动关闭连接
                        println("WebSocket connection timed out")
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
                            ConsoleOutputV2.systemPrint("新设备接入/I： Device $device  AppVersion: $app_version ClientVersion: $client_version")
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
                                text = text.replace("\n", " ").replace(" ------------ ", "")+"\r\n"
                            }
                            ConsoleOutputV2.println(device, text)
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
                   logI("设备离线： Device $device")
                }
            }
            .listen(port) { http ->
                if (http.succeeded()) {
                    startPromise.complete()
//                    ConsoleOutputV2.systemPrint("服务器启动/I:  ${VertxServer.getServerIpAddress()}:$port")
                    VertxServer.isStart = true
                } else {
                    startPromise.fail(http.cause())
                    logE("服务器无法启动在端口$port",http.cause())
                }
            }


        router.route("/").handler {
            println("/\t" + it.body().asString())
        }
        router.route("/exec").handler {
            println("/exec\t" + it.body().asString())
        }
        router.route("/receive").handler(BodyHandler.create()).handler {
            val jsonObject = it.body().asJsonObject()
            VertxServer.content[jsonObject.getString("message")] = jsonObject
            println("receive: $jsonObject")
            it.response().end("ok")
        }

        router.route("/demo").handler { cnt ->
//            VertxServer.Command.runProject("./build/webview.zip")
//            VertxServer.Command.saveProjectToRoot("./build/webview.zip")
//            VertxServer.Command.rerunJS("./build/test.js")
//            VertxServer.Command.runJS("./build/test.js")
//            VertxServer.Command.runJsByString(content = "log(123)")
        }
        router.route("/stop").handler {
//            VertxServer.Command.stop("./build/test.js")
            VertxServer.Command.saveJS("./build/test.js")
//            VertxServer.Command.stopAll()
            it.response().end("21")
        }
    }

    fun String.asJsonObject(): JsonObject = JsonObject(this)
}
