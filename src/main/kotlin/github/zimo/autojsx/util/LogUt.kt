package github.zimo.autojsx.util

import github.zimo.autojsx.server.ConsoleOutput

fun logE(msg: Any?, e: Throwable? = null) {
    ConsoleOutput.systemPrint("错误/E: $msg ${(if (e != null) "\r\n" else "") + e?.caseString()}")
}

fun logE(msg: Any?) {
    ConsoleOutput.systemPrint("错误/E: $msg")
}

fun logI(msg: Any?) {
    ConsoleOutput.systemPrint("信息/I: $msg")
}

fun logD(msg: Any?) {
    ConsoleOutput.systemPrint("测试/D: $msg")
}

fun logV(msg: Any?) {
    ConsoleOutput.systemPrint("调试/V: $msg")
}

fun logW(msg: Any?) {
    ConsoleOutput.systemPrint("警告/W: $msg")
}

fun logW(msg: Any?, e: Throwable?) {
    ConsoleOutput.systemPrint("错误/W: $msg ${(if (e != null) "\r\n" else "") + e?.caseString()}")
}