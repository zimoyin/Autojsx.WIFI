package github.zimo.autojsx.server

import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.application.ApplicationManager


object ConsoleOutputV2 {
    private val map: HashMap<String, ArrayList<Message>> = HashMap()
    var console: ConsoleViewImpl? = null
    var currentDevice: String? = "所有设备"
    var isInitOutput = false
    var level0: String = "V"

    private fun levelI(): Int {
        var result = 0
        when (level0) {
            "V" -> result = 0
            "D" -> result = 1
            "I" -> result = 2
            "W" -> result = 3
            "E" -> result = 4
        }
        return result
    }

    fun setLevel(level: String) {
        if (level == level0) return
        level0 = level
        isInitOutput = false
    }

    fun println(device: String, message: String) {
        var list = map.get(device)
        if (list == null) {
            list = ArrayList()
            map[device] = list
        }
        list = limitSize(list)
        val msg = parse(device, message + "\r\n")
        list.add(msg)
        print(msg)
    }

    fun print(device: String, message: String) {
        var list = map.get(device)
        if (list == null) {
            list = ArrayList()
            map[device] = list
        }
        list = limitSize(list)
        val msg = parse(device, message + "\r\n")
        list.add(msg)
        print(msg)
    }

    /**
     *         consoleView.clear()//清空控制台，注意如果没有被写入控制台则无法清空
     *         consoleView.print("普通输出内容的默认样式\n", ConsoleViewContentType.NORMAL_OUTPUT)
     *         consoleView.print("错误输出内容的样式，通常以红色显示。\n", ConsoleViewContentType.ERROR_OUTPUT)
     *         consoleView.print("系统输出内容的样式，用于显示系统信息，通常以灰色显示。\n", ConsoleViewContentType.SYSTEM_OUTPUT)
     *         consoleView.print("黄色\n", ConsoleViewContentType.LOG_INFO_OUTPUT)
     *         consoleView.print("蓝色\n", ConsoleViewContentType.LOG_VERBOSE_OUTPUT)
     *         consoleView.print("用于显示调试输出内容的样式，通常以青色显示。\n", ConsoleViewContentType.LOG_DEBUG_OUTPUT)
     *         consoleView.print("用于显示警告输出内容的样式，通常以黄色显示。\n", ConsoleViewContentType.LOG_WARNING_OUTPUT)
     *         consoleView.print("用于显示错误输出内容的样式，通常以红色显示。\n", ConsoleViewContentType.LOG_ERROR_OUTPUT)
     */

    private fun print(message: Message) {
        var list = map.get("所有设备")
        if (list == null) {
            list = ArrayList()
            map["所有设备"] = list
        }
        list = limitSize(list)
        list.add(message)
        update()
    }

    fun systemPrint(message: String) {
        val msg = parse("系统日志", message + "\r\n")
        print(msg)
    }


    private fun parse(device: String, message: String): Message {
        var level: ConsoleViewContentType = ConsoleViewContentType.LOG_VERBOSE_OUTPUT
        var levelI: Int = 0

        val indexOfSlash = message.indexOf('/')
        if (indexOfSlash != -1 && indexOfSlash < message.length - 1) {
            val result = message.substring(indexOfSlash + 1, indexOfSlash + 2)
            when (result) {
                "V" -> {
                    level = ConsoleViewContentType.LOG_VERBOSE_OUTPUT
                    levelI = 0
                }

                "D" -> {
                    level = ConsoleViewContentType.LOG_DEBUG_OUTPUT
                    levelI = 1
                }

                "I" -> {
                    level = ConsoleViewContentType.LOG_VERBOSE_OUTPUT
                    levelI = 2
                }

                "W" -> {
                    level = ConsoleViewContentType.LOG_INFO_OUTPUT
                    levelI = 3
                }

                "E" -> {
                    level = ConsoleViewContentType.LOG_ERROR_OUTPUT
                    levelI = 4
                }
                else ->{
                    level = ConsoleViewContentType.LOG_VERBOSE_OUTPUT
                    levelI = 0
                }
            }
        }

        return Message(
            level,
            levelI,
            device,
            message,
        )
    }

    private fun limitSize(list: java.util.ArrayList<Message>): java.util.ArrayList<Message> {
        var list1 = list
        if (list1.size >= 100000) {
            list1 = ArrayList<Message>().apply {
                for (i in 50000..list1.size) {
                    add(list1[i])
                }
            }
        }
        return list1
    }

    fun update() {
        val device = Devices.currentDevice
        if (currentDevice == device) {
            if (!isInitOutput) {
                if (console != null) {
                    clearConsole()
                    isInitOutput = true
                    map[currentDevice]?.forEach {
                        if (it.levelI >= levelI()) console!!.print("[${it.device}] ${it.message}", it.level)
                    }
                }
            } else {
                if (console != null) {
                    map[currentDevice]?.last()?.let {
                        if (it.levelI >= levelI()) console!!.print("[${it.device}] ${it.message}", it.level)
                    }
                }
            }
        } else {
            currentDevice = device
            if (console != null) {
                clearConsole()
                isInitOutput = true
                map[currentDevice]?.forEach {
                    if (it.levelI >= levelI()) console!!.print("[${it.device}] ${it.message}", it.level)
                }
            }
        }
        flush()
    }

    private fun flush() {
        ApplicationManager.getApplication().invokeLater {
            //刷新控制台
            console?.performWhenNoDeferredOutput {
                console!!.flushDeferredText()
            }
        }
    }

    fun clear() {
        clearConsole()
        if (currentDevice == "所有设备") map.clear()
        else map.remove(currentDevice)
    }

    private fun clearConsole() {
        console?.clear()
        flush()
    }

    fun toEnd() {
        console?.scrollToEnd()
    }


    data class Message(
        val level: ConsoleViewContentType,
        val levelI: Int,
        val device: String,
        val message: String,
    )
}