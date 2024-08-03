package lib.module

import kotlin.js.Console
import kotlin.js.Promise

/**
 * version {string} | {number} Auto.js的版本或版本号
 * 表示此脚本需要Auto.js版本达到指定版本才能运行。
 */
external fun requiresAutojsVersion(val0: dynamic): Boolean

/**
 * 表示此脚本需要Android API版本达到指定版本才能运行。
 */
external fun requiresApi(val0: dynamic): Boolean


/**
 * 通过应用名称启动应用。如果该名称对应的应用不存在，则返回false; 否则返回true。如果该名称对应多个应用，则只启动其中某一个。
 */
external fun launchApp(appName: String): Boolean

/**
 * 通过应用包名启动应用。如果该包名对应的应用不存在，则返回false；否则返回true。
 */
external fun launch(packageName: String): Boolean

/**
 * 通过应用包名启动应用。如果该包名对应的应用不存在，则返回false；否则返回true。
 */
external fun launchPackage(packageName: String): Boolean

/**
 * 获取应用名称对应的已安装的应用的包名。如果该找不到该应用，返回null；如果该名称对应多个应用，则只返回其中某一个的包名。
 */
external fun getPackageName(appName: String): String

/**
 * 获取应用包名对应的已安装的应用的名称。如果该找不到该应用，返回null。
 */
external fun getAppName(packageName: String): String

/**
 * 打开应用的详情页(设置页)。如果找不到该应用，返回false; 否则返回true。
 */
external fun openAppSetting(packageName: String): Boolean


/**
 * 打印到控制台，并带上换行符。 可以传入多个参数，第一个参数作为主要信息，其他参数作为类似于 printf(3) 中的代替值（参数都会传给 util.format()）。
 */
external fun log(data: Any, vararg args: Any?): Unit

/**
 * 输出函数
 * 相当于log(text)。
 */
external fun print(message: Any): Unit


/* 基于坐标的触摸模拟 */

/**
 * 设置脚本坐标点击所适合的屏幕宽高。如果脚本运行时，屏幕宽度不一致会自动放缩坐标。
 */
external fun setScreenMetrics(width: Int, height: Int): Unit

/* 安卓7.0以上的触摸和手势模拟 */

/**
 * Android7.0以上
 *
 * 模拟点击坐标(x, y)大约150毫秒，并返回是否点击成功。只有在点击执行完成后脚本才继续执行。
 */
external fun click(x: Int, y: Int): Unit

/**
 * Android7.0以上
 *
 * 模拟长按坐标(x, y), 并返回是否成功。只有在长按执行完成（大约600毫秒）时脚本才会继续执行。
 */
external fun longClick(x: Int, y: Int): Unit

/**
 * Android7.0以上
 *
 * 模拟按住坐标(x, y), 并返回是否成功。只有按住操作执行完成时脚本才会继续执行。
 *
 * 如果按住时间过短，那么会被系统认为是点击；如果时长超过500毫秒，则认为是长按。
 */
external fun press(x: Int, y: Int, duration: Int): Unit

/**
 * 模拟从坐标(x1, y1)滑动到坐标(x2, y2)，并返回是否成功。只有滑动操作执行完成时脚本才会继续执行。
 */
external fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Int): Boolean

/**
 * 生成gesture函数所需要的一个坐标点。
 */
fun gesturePoint(x1: Int, y1: Int): Array<Int> {
    return arrayOf(x1, y1)
}

/**
 * 模拟手势操作。
 * 例如gesture(1000, gesturePoint(0, 0), gesturePoint(500, 500), gesturePoint(500, 1000)
 * 为模拟一个从(0, 0)到(500, 500)到(500, 100)的手势操作，时长为2秒。
 * @duration {number} 手势的时长
 */
external fun gesture(duration: Int, point1: Array<Int>, point2: Array<Int>, vararg points: Array<Int>): Unit

sealed class Gesture {
    /**
     * @delay 为延迟多久(毫秒)才执行该手势
     * @duration 为手势执行时长
     */
    data class Type1(val delay: Int, val duration: Int, val c: Array<Int>, val d: Array<Int>) : Gesture()
    data class Type2(val a: Int, val b: Array<Int>, val c: Array<Int>) : Gesture()
}

@Deprecated("推荐使用 gesturesByKotlin，而不是原生 js 的方法")
val gestures = js("gestures")

/**
 * 同时模拟多个手势。每个手势的参数为[delay, duration, 坐标], delay为延迟多久(毫秒)才执行该手势；duration为手势执行时长；坐标为手势经过的点的坐标。其中delay参数可以省略，默认为0。
 */
fun gesturesByKotlin(vararg param: Gesture.Type1) {
    var paramStr = ""
    param.forEach {
        if (paramStr.isNotEmpty()) paramStr += ","
        paramStr += buildString {
            append("[")
            append(it.delay).append(", ").append(it.duration)
            append(",")
            append("[").append(it.c[0]).append(", ").append(it.c[1]).append("]")
            append(",")
            append("[").append(it.d[0]).append(", ").append(it.d[1]).append("]")
            append("]")
        }
    }

    eval("gestures($paramStr);")
}


/**
 * 需要Root权限
 *
 * 实验API，请勿过度依赖
 *
 * 点击位置(x, y), 您可以通过"开发者选项"开启指针位置来确定点击坐标。
 */
external fun Tap(x: Int, y: Int): Unit

/**
 * 需要Root权限
 *
 * 实验API，请勿过度依赖
 *
 * 滑动。从(x1, y1)位置滑动到(x2, y2)位置。
 */
external fun Swipe(x1: Int, x2: Int, y1: Int, y2: Int, duration: Int? = definedExternally): Unit


/**
 * 显示一个只包含“确定”按钮的提示对话框。直至用户点击确定脚本才继续运行。
 */
external fun alert(title: String, content: String? = definedExternally): Unit

/**
 * UI模式
 *
 * 显示一个只包含“确定”按钮的提示对话框。直至用户点击确定脚本才继续运行。
 *
 * 在ui模式下该函数返回一个Promise。
 */
external fun alert(title: String, content: String? = definedExternally, callback: (() -> Unit)?): Promise<Unit>

/**
 * 显示一个包含“确定”和“取消”按钮的提示对话框。如果用户点击“确定”则返回 true ，否则返回 false 。
 */
external fun confirm(title: String, content: String? = definedExternally): Boolean

/**
 * UI模式
 *
 * 显示一个包含“确定”和“取消”按钮的提示对话框。如果用户点击“确定”则返回 true ，否则返回 false 。
 *
 * 在ui模式下该函数返回一个Promise。
 */
external fun confirm(
    title: String,
    content: String? = definedExternally,
    callback: ((value: Boolean) -> Unit)?
): Promise<Boolean>

/**
 * 显示一个包含输入框的对话框，等待用户输入内容，并在用户点击确定时将输入的字符串返回。如果用户取消了输入，返回null。
 * title {string} 对话框的标题。
 * prefill {string} 输入框的初始内容，可选，默认为空。
 * callback {Function} 回调函数，可选。当用户点击确定时被调用,一般用于ui模式。
 */
external fun rawInput(title: String, prefill: String? = definedExternally): String

/**
 * UI模式
 *
 * 显示一个包含输入框的对话框，等待用户输入内容，并在用户点击确定时将输入的字符串返回。如果用户取消了输入，返回null。
 * title {string} 对话框的标题。
 * prefill {string} 输入框的初始内容，可选，默认为空。
 * callback {Function} 回调函数，可选。当用户点击确定时被调用,一般用于ui模式。
 */
external fun rawInput(
    title: String,
    prefill: String? = definedExternally,
    callback: ((value: String) -> Unit)?
): Promise<String>


/**
 *
 * @mode {string} 文件打开模式，包括:
 *      r: 只读文本模式。该模式下只能对文件执行文本读取操作。
 *      w: 只写文本模式。该模式下只能对文件执行文本覆盖写入操作。
 *      a: 附加文本模式。该模式下将会把写入的文本附加到文件末尾。
 *      rw: 随机读写文本模式。该模式下将会把写入的文本附加到文件末尾。
 *      目前暂不支持二进制模式，随机读写模式。
 * @encoding {string} 字符编码。
 * @bufferSize {number} 文件读写的缓冲区大小。
 */
external fun open(
    path: String,
    mode: String /* 'r' */,
    encoding: String? = definedExternally,
    bufferSize: Int? = definedExternally
): PReadableTextFile

external fun sleep(n: Int): Unit

external fun currentPackage(): String

external fun currentActivity(): String

external fun setClip(test: String): Unit

external fun getClip(): String

external fun toast(message: String): Unit

external fun toastLog(message: String): Unit

external fun waitForActivity(activity: String, period: Int? = definedExternally): Unit

external fun waitForPackage(packageName: String, period: Int? = definedExternally): Unit

external fun exit(): Unit

external fun random(): Int;
external fun random(min: Int, max: Int): Int;

@Suppress("NOTHING_TO_INLINE")
inline fun Console.trace(message: dynamic, vararg args: Any?): Unit = asDynamic().trace(message) as Unit

/* 全局按键 */
external fun back(): Boolean
external fun home(): Boolean
external fun powerDialog(): Boolean
external fun notifications(): Boolean
external fun quickSettings(): Boolean
external fun recents(): Boolean
external fun splitScreen(): Boolean
external fun Home(): Unit
external fun Back(): Unit
external fun Power(): Unit
external fun Menu(): Unit
external fun VolumeUp(): Unit
external fun VolumeDown(): Unit
external fun Camera(): Unit
external fun Up(): Unit
external fun Down(): Unit
external fun Left(): Unit
external fun Right(): Unit
external fun OK(): Unit
external fun Text(text: String): Unit
external fun KeyCode(code: String): Unit
external fun KeyCode(code: Int): Unit

// 'fast' | 'normal'
external fun auto(mode: String = definedExternally): Unit

external fun selector(): UiSelector;
external fun click(text: String, index: Int? = definedExternally): Boolean
external fun click(left: Int, top: Int, bottom: Int, right: Int): Boolean
external fun longClick(text: String, index: Int? = definedExternally): Boolean
external fun scrollUp(index: Int? = definedExternally): Boolean
external fun scrollDown(index: Int? = definedExternally): Boolean
external fun setText(text: String): Boolean
external fun setText(index: Int, text: String): Boolean
external fun input(text: String): Boolean
external fun input(index: Int, text: String): Boolean
