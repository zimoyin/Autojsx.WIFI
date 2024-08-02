package lib.module

import kotlin.js.Promise

@JsName("threads")

external object Thread {
    interface CurrentThread {
        fun interrupt()
        fun join(timeout: Int? = definedExternally)
        fun isAlive(): Boolean
        fun waitFor()

        @Deprecated("use setTimeoutByKotlin")
        fun setTimeout(callback: (Array<Any>) -> Unit, delay: Int, vararg args: Any): Int

        @Deprecated("use setIntervalByKotlin")
        fun setInterval(callback: (Array<Any>) -> Unit, delay: Int, vararg args: Any): Int

        @Deprecated("use setImmediateByKotlin")
        fun setImmediate(callback: (Array<Any>) -> Unit, vararg args: Any): Int
        fun clearInterval(id: Int)
        fun clearTimeout(id: Int)
        fun clearImmediate(id: Int)
    }

    fun start(action: () -> Unit): CurrentThread
    fun shutDownAll()
    fun currentThread(): CurrentThread
    fun disposable(): Any

    /**
     * 获取一个整数原子变量，保证线程共享变量间的数据安全
     */
    fun atomic(initialValue: Int? = definedExternally): Any

    /**
     * 新建一个可重入锁
     */
    fun lock(): Lock

    interface Lock {
        fun lock()
        fun unlock()
    }
}

fun Thread.getName(): String {
    return js("java.lang.Thread.currentThread().getName()") as String
}

fun Thread.CurrentThread.getName(): String {
    return js("java.lang.Thread.currentThread().getName()") as String
}

fun Thread.sleep(n: Int) {
    lib.module.sleep(n)
}

fun Thread.CurrentThread.setTimeoutByKotlin(delay: Int, vararg args: Any, callback: (Array<Any>) -> Unit): Int {
    return this.setTimeout(callback, delay, *args)
}

fun Thread.CurrentThread.setIntervalByKotlin(delay: Int, vararg args: Any, callback: (Array<Any>) -> Unit): Int {
    return this.setInterval(callback, delay, *args)
}

fun Thread.CurrentThread.setImmediateByKotlin(vararg args: Any, callback: (Array<Any>) -> Unit): Int {
    return this.setImmediate(callback, *args)
}

/**
 *
 * !!! 不推荐使用 Kotlin 调用 Promise.coroutine 等方法，请使用 Kotlin 协程 或者 使用 eval/js 使用 原生 JS 调用  !!!
 *
 * bluebird 协程
 * 对比 Kotlin 协程，可能性能更高一些
 */
@JsName("Promise")
external object Promise {

    /**
     * Generator 函数写法
     * 例如：
     * let main = Promise.coroutine(function*(size) {
     *     for (var i = 0; i < size; i++) {
     *         yield Promise.delay(1000);
     *         log(i);
     *     }
     *     log('end')
     * })
     * main(10);//在控制台每秒输出一个数字
     *
     */
    @Deprecated("Use coroutineByKotlin")
    fun  coroutine(value:dynamic): Promise<dynamic>
    fun <T> resolve(value:T): Promise<dynamic>

    /**
     * 函数的参数为yield 的导出，反回值为yield的反回
     */
    fun addYieldHandler(handler: (dynamic) -> dynamic): dynamic
}
fun lib.module.Promise.coroutineByKotlin(action: (it: dynamic) -> dynamic): dynamic {
    return eval(
        """
        Promise.coroutine(function*(s) {
            return action(s);
        })
    """.trimIndent()
    )
}
/**
 * Adds a synchronization lock to the function `func` and returns it as a new function.
 * @param callback Function to be locked
 * @return A new function with synchronization lock applied
 */
external fun sync(callback: (ArrayList<Any>) -> Any): (ArrayList<Any>) -> Any
