package lib.module

@JsName("events")

external object Events {

    /**
     * 按键事件接口
     */
    interface KeyEvent {
        fun getAction(): dynamic
        fun getKeyCode(): Int
        fun getEventTime(): Double
        fun getDownTime(): Double
        fun keyCodeToString(keyCode: Int): String
    }

    fun emitter(): EventEmitter

    fun observeKey(): Unit

    fun setKeyInterceptionEnabled(key: Keys, enabled: Boolean): Unit

    fun setKeyInterceptionEnabled(enabled: Boolean): Unit

    fun onKeyDown(keyName: Keys, listener: (e: KeyEvent) -> Unit): Unit

    fun onceKeyUp(keyName: Keys, listener: (e: KeyEvent) -> Unit): Unit

    fun removeAllKeyDownListeners(keyName: Keys): Unit

    fun removeAllKeyUpListeners(keyName: Keys): Unit

    fun observeTouch(): Unit

    fun setTouchEventTimeout(timeout: Double): Unit

    fun getTouchEventTimeout(): Double

    fun onTouch(listener: (point: Point) -> Unit): Unit

    fun removeAllTouchListeners(): Unit

    fun on(event: String /* 'key' | 'key_down' | 'key_up' */, listener: (keyCode: Int, e: KeyEvent) -> Unit): Unit

    fun on(event: String /* 'exit' */, listener: () -> Unit): Unit

    fun observeNotification(): Unit

    fun observeToast(): Unit

    /**
     * 系统Toast对象
     */
    interface Toast {
        /**
         * 获取Toast的文本内容
         */
        fun getText(): String

        /**
         * 获取发出Toast的应用包名
         */
        fun getPackageName(): String
    }

    fun onToast(listener: (toast: Toast) -> Unit): Unit

    /**
     * 通知对象，可以获取通知详情，包括通知标题、内容、发出通知的包名、时间等，也可以对通知进行操作，比如点击、删除。
     */
    interface Notification {
        var number: Int
        var `when`: Double
        fun getPackageName(): String
        fun getTitle(): String
        fun getText(): String
        fun click(): Unit
        fun delete(): Unit
    }

    fun on(event: String /* 'notification' */, listener: (notification: Notification) -> Unit): Unit
}

/**
 * 按键事件中所有可用的按键名称
 */
external enum class Keys {
    home,
    back,
    menu,
    volume_up,
    volume_down
}

external interface EventEmitter {
    var defaultMaxListeners: Int
    fun addListener(eventName: String, listener: (args: Array<Any>) -> Unit): EventEmitter
    fun emit(eventName: String, vararg args: Any): Boolean
    fun eventNames(): Array<String>
    fun getMaxListeners(): Int
    fun listenerCount(eventName: String): Int
    fun on(eventName: String, listener: (args: Array<Any>) -> Unit): EventEmitter
    fun once(eventName: String, listener: (args: Array<Any>) -> Unit): EventEmitter
    fun prependListener(eventName: String, listener: (args: Array<Any>) -> Unit): EventEmitter
    fun prependOnceListener(eventName: String, listener: (args: Array<Any>) -> Unit): EventEmitter
    fun removeAllListeners(eventName: String? = definedExternally): EventEmitter
    fun removeListener(eventName: String, listener: (args: Array<Any>) -> Unit): EventEmitter
    fun setMaxListeners(n: Int): EventEmitter
}
