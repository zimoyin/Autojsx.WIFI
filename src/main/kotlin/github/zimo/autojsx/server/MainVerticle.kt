package github.zimo.autojsx.server

import github.zimo.autojsx.server.VertxServer.devices
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
                println("========================================")
                println("新设备进入")
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
                            //save ws
                            devices[device] = ws
                        }

                        "ping" -> {
                            // 收到消息，重置定时器
                            vertx.cancelTimer(timerId);
                            returnData.put("data", jsonObject.getLong("data"))
                            returnData.put("type", "pong")
                        }

                        "log" -> {
                            println(jsonObject.getJsonObject("data").getString("log"))
                        }
                    }
                    if (!jsonObject.getString("type").equals("ping") && !jsonObject.getString("type").equals("log")) {
                        println("request: $jsonObject")
                        println("response: $returnData")
                        println("-------------------------------------")
                    }
                    if (!returnData.isEmpty) ws.writeTextMessage(returnData.toString())
                }

                ws.closeHandler {
                    //remove device
                    devices.remove("device")
                    println("WebSocket connection closed")
                }
            }
            .listen(port) { http ->
                if (http.succeeded()) {
                    startPromise.complete()
                    println("HTTP server started on InetAddress ${VertxServer.getServerIpAddress()}:$port")
                    VertxServer.isStart = true
                } else {
                    startPromise.fail(http.cause());
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
            it.response().end("ok")
        }

        router.route("/demo").handler {
//            VertxServer.Command.runProject("./build/webview.zip")
//            VertxServer.Command.saveProjectToRoot("./build/webview.zip")
//            VertxServer.Command.rerunJS("./build/test.js")
//            VertxServer.Command.runJS("./build/test.js")
            VertxServer.Command.runJsByString(content = "log(123)")
            it.response().end("21")
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
