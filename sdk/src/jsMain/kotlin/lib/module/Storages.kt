package lib.module

@JsName("storages")

external object Storages {

    /**
     * 创建或获取一个存储对象。
     * @param name 存储的名称
     * @return Storage 对象
     */
    fun create(name: String): Storage

    /**
     * 删除一个存储。
     * @param name 存储的名称
     * @return 是否成功删除
     */
    fun remove(name: String): Boolean
}

/**
 * Storage 接口，提供存储键值对的方法。
 */
external interface Storage {

    /**
     * 获取指定键的值，如果键不存在则返回默认值。
     * @param key 键名
     * @param defaultValue 默认值，可选
     * @return 键对应的值
     */
    fun <T> get(key: String, defaultValue: T? = definedExternally): T

    /**
     * 存储一个键值对。
     * @param key 键名
     * @param value 值
     */
    fun <T> put(key: String, value: T)

    /**
     * 删除指定键的值。
     * @param key 键名
     */
    fun remove(key: String)

    /**
     * 判断存储中是否包含指定键。
     * @param key 键名
     * @return 是否包含指定键
     */
    fun contains(key: String): Boolean

    /**
     * 清空存储中的所有键值对。
     */
    fun clear()
}
