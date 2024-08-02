package github.zimo.autojsx.server

import github.zimo.autojsx.util.logE
import github.zimo.autojsx.util.logI
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.json.JsonObject
import java.net.InetAddress
import java.net.NetworkInterface


object VertxServer {
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
        }.apply {
            vertx = null
        }
        selectDevicesWs.clear()
        devicesWs.clear()
        content.clear()
        isStart = false
        return close
    }

    fun isActivity(): Boolean = vertx != null

    fun devicesEmpty(devices: HashMap<String, ServerWebSocket> = selectDevicesWs) {
        if (devices.isEmpty()) {
            ConsoleOutput.systemPrint("操作警告/W: 未选择任何设备,无法执行脚本")
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
}