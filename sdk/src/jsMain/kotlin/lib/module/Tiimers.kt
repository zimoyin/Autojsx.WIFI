package lib.module

/**
 *
 * @author : zimo
 * @date : 2024/07/30
 */
//TODO

@Deprecated("use setTimeoutByKotlin")
external fun setTimeout(callback: () -> Unit, delay: Int, vararg args: Any)

/**
 * 预定在 delay 毫秒之后执行的单次 callback。 返回一个用于 clearTimeout() 的 id。
 * callback 可能不会精确地在 delay 毫秒被调用。 Auto.js 不能保证回调被触发的确切时间，也不能保证它们的顺序。 回调会在尽可能接近所指定的时间上调用。
 * 当 delay 小于 0 时，delay 会被设为 0。
 */
fun setTimeoutByKotlin(delay: Int, vararg args: Any, callback: () -> Unit) {
    setTimeout(callback, delay, *args)
}

fun setTimeoutByKotlin(delay: Int, vararg args: Any) {
    setTimeout(fun() {}, delay, *args)
}

@Deprecated("use setIntervalByKotlin")
external fun setInterval(callback: () -> Unit, delay: Int, vararg args: Any)

/**
 * 预定每隔 delay 毫秒重复执行的 callback。 返回一个用于 clearInterval() 的 id。
 *
 * 当 delay 小于 0 时，delay 会被设为 0。
 */
fun setIntervalByKotlin(delay: Int, vararg args: Any, callback: () -> Unit) {
    setInterval(callback, delay, *args)
}


@Deprecated("use setImmediateByKotlin")
external fun setImmediate(callback: () -> Unit, vararg args: Any): Int

/**
 * 预定立即执行的 callback，它是在 I/O 事件的回调之后被触发。 返回一个用于 clearImmediate() 的 id。
 * 当多次调用 setImmediate() 时，callback 函数会按照它们被创建的顺序依次执行。 每次事件循环迭代都会处理整个回调队列。 如果一个立即定时器是被一个正在执行的回调排入队列的，则该定时器直到下一次事件循环迭代才会被触发。
 * setImmediate()、setInterval() 和 setTimeout() 方法每次都会返回表示预定的计时器的id。 它们可用于取消定时器并防止触发。
 */
fun setImmediateByKotlin(vararg args: Any, callback: () -> Unit): Int {
    return setImmediate(callback, *args)
}

/**
 * 取消一个由 setInterval() 创建的循环定时任务。
 */
external fun clearInterval(id: Int)

/**
 * 取消一个由 setTimeout() 创建的定时任务。
 */
external fun clearTimeout(id: Int)

/**
 * 取消一个由 setImmediate() 创建的 Immediate 对象。
 */
external fun clearImmediate(id: Int)
