package github.zimo.autojsx.server

import java.net.NetworkInterface

@Deprecated("测试方法")
fun main() {
//  VertxCommandServer.start(8080)
  println(getServerIpAddress())
}

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