package lib.module

@JsName("sensors")

external object Sensors {

    /**
     * 传感器事件发射器接口
     */
    interface SensorEventEmitter {
        /**
         * 当传感器值改变时触发的事件
         * @param eventName 事件名称
         * @param callback 回调函数
         */
        fun on(eventName: String /* 'change' */, callback: (ArrayList<Number> ) -> Unit): Unit

        /**
         * 当传感器精度改变时触发的事件
         * @param eventName 事件名称
         * @param callback 回调函数
         */
        fun on(eventName: String /* 'accuracy_change' */, callback: (accuracy: Number) -> Unit): Unit
    }

    /**
     * 当不支持传感器时触发的事件
     * @param eventName 事件名称
     * @param callback 回调函数
     */
    fun on(eventName: String /* 'unsupported_sensor' */, callback: (sensorName: String) -> Unit): Unit

    /**
     * 注册传感器
     * @param sensorName 传感器名称
     * @param delay 传感器数据获取的延迟
     * @return 传感器事件发射器
     */
    fun register(sensorName: String, delay: Delay? = definedExternally): SensorEventEmitter

    /**
     * 注销传感器
     * @param emitter 传感器事件发射器
     */
    fun unregister(emitter: SensorEventEmitter): Unit

    /**
     * 注销所有传感器
     */
    fun unregisterAll(): Unit

    /**
     * 是否忽略不支持的传感器
     */
    var ignoresUnsupportedSensor: Boolean

    /**
     * 传感器数据获取延迟枚举
     */
    enum class Delay {
        normal,
        ui,
        game,
        fastest
    }
}
