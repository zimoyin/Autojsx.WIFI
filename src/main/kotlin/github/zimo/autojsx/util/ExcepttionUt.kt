package github.zimo.autojsx.util

import java.io.PrintWriter
import java.io.StringWriter


fun  Throwable.caseString(): String {
    var sw: StringWriter?=null
    var pw: PrintWriter?=null
    return try {
        sw = StringWriter()
        pw = PrintWriter(sw)
        this.printStackTrace(pw)

        sw.toString()
    } catch (e2: Exception) {
        "[严重]无法格式化运行时的异常"
    }finally {
        sw?.close()
        pw?.close()
    }
}