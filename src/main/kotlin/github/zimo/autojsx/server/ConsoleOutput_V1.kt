package github.zimo.autojsx.server

import com.intellij.ui.components.JBTextArea

object ConsoleOutput_V1 {
    var console: JBTextArea? = null
    var currentDevice: String? = "所有设备"
    val rows = 80

    private val map: HashMap<String, ArrayList<String>> = HashMap()

    fun print(device: String, message: String) {
        var list = map.get("所有设备")
        if (list == null) {
            list = ArrayList()
            map["所有设备"] = list
        }
        list.add(message)
        updateConsole(message)
    }

    fun print(message: String) {
        var list = map.get("所有设备")
        if (list == null) {
            list = ArrayList()
            map["所有设备"] = list
        }
        list.add(message)
        updateConsole(message)
    }

    fun clear(device: String = Devices.currentDevice ?: "所有设备") {
        map.remove(device)
        updateConsole()
    }

    fun clearAll() {
        map.clear()
        updateConsole()
    }

    fun updateConsole(message: String) {
        val device = Devices.currentDevice
        if (device == currentDevice) {
            if (console != null) {
//                console!!.append(message)
                console!!.text = ""
                map[device]?.forEach {
                    console!!.append(it)
                }
                for (i in 0 until rows) {
                    console!!.append("\n")
                }
            }
        } else {
            currentDevice = device
            if (console != null) {
                console!!.text = ""
                map[device]?.forEach {
                    console!!.append(it)
                }
                for (i in 0 until rows) {
                    console!!.append("\n")
                }
            }
        }
    }

    fun updateConsole() {
        val device = Devices.currentDevice
        if (device == currentDevice) {
            if (console != null) {
                map[device]?.forEach {
//                    console!!.append(it)
                    console!!.text = ""
                    map[device]?.forEach {
                        console!!.append(it)
                    }
                    for (i in 0 until rows) {
                        console!!.append("\n")
                    }
                }
            }
        } else {
            currentDevice = device
            if (console != null) {
                console!!.text = ""
                map[device]?.forEach {
                    console!!.append(it)
                }
                for (i in 0 until rows) {
                    console!!.append("\n")
                }
            }
        }
    }
}