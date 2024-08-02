package lib.kotlin

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

private var COUNT = 0


/**
 * 线程池调度器
 */
class ThreadPoolDispatcher : CoroutineDispatcher() {
    private val executors = js("java.util.concurrent.Executors.newFixedThreadPool(8)")

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        executors.execute {
            block.run()
        }
    }

    fun shutdown() {
        executors.shutdown()
    }
}

/**
 * 创建一个协程，并返回一个 [Job] 对象，用于管理协程的生命周期。该协程是作用于外部线程中，注意外部线程不要阻塞
 * @param dispatcher 协程调度器，默认为 [Dispatchers.Main]
 * @param name 协程的名称，默认为 "Main Coroutine $COUNT"
 * @param callback 协程执行的代码块
 */
fun launch(
    dispatcher: CoroutineContext = Dispatchers.Main,
    name: String = "Main Coroutine $COUNT",
    callback: suspend CoroutineScope.() -> Unit
): Job {
    COUNT++
    return CoroutineScope(dispatcher + CoroutineName(name)).launch {
        callback()
    }
}

/**
 * 创建一个协程，并返回一个 [Deferred] 对象，用于获取执行结果。使用 await 可以挂起协程并等待结果.该协程是作用于外部线程中，注意外部线程不要阻塞
 * 注意调用 await 后不会阻塞线程但是会阻塞协程。
 * @param dispatcher 协程调度器，默认为 [Dispatchers.Default]
 * @param name 协程的名称，默认为 "Default Coroutine $COUNT"
 * @param callback 协程执行的代码块
 */
fun <T> async(
    dispatcher: CoroutineContext = Dispatchers.Main,
    name: String = "Default Coroutine $COUNT",
    callback: suspend () -> T
): Deferred<T> {
    COUNT++
    return CoroutineScope(dispatcher + CoroutineName(name)).async {
        callback()
    }
}

/**
 * 创建一个允许等待所有的协程任务执行完毕的列表
 * 调用 result.jonAll() 来等待
 */
fun coordination(
    dispatcher: CoroutineContext = Dispatchers.Default,
    name: String = "Default Coroutine $COUNT",
    list: List<suspend CoroutineScope.() -> Unit>,
): ArrayList<Job> {
    val jobs: ArrayList<Job> = ArrayList<Job>()
    list.forEach {
        CoroutineScope(dispatcher + CoroutineName(name)).launch {
            COUNT++
            it()
        }.apply {
            jobs.add(this)
        }
    }
    return jobs
}

val CoroutineScope.name: String
    get() = coroutineContext[CoroutineName]?.name ?: "Unknown"