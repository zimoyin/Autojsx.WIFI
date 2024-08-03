package lib.kotlin

import kotlinx.serialization.json.*
import kotlin.reflect.KClass

/**
 * Js 对象内置方法，以及 Kotlin 对其拓展的方法
 * @author : zimo
 * @date : 2024/07/31
 */
@JsName("Object")
external object JSObject {
    fun assign(target: dynamic, vararg sources: dynamic): dynamic
    fun create(proto: dynamic, propertiesObject: dynamic): dynamic
    fun create(proto: dynamic): dynamic
    fun defineProperty(obj: dynamic, prop: String, descriptor: dynamic): dynamic
    fun defineProperties(obj: dynamic, properties: dynamic): dynamic
    fun entries(obj: dynamic): dynamic
    fun freeze(obj: dynamic): dynamic
    fun fromEntries(iterable: dynamic): dynamic
    fun getOwnPropertyDescriptor(obj: dynamic, prop: String): dynamic
    fun getOwnPropertyDescriptors(obj: dynamic): dynamic
    fun getOwnPropertyNames(obj: dynamic): dynamic
    fun getOwnPropertySymbols(obj: dynamic): dynamic
    fun getPrototypeOf(obj: dynamic): dynamic
    fun Is(value1: dynamic, value2: dynamic): Boolean
    fun isExtensible(obj: dynamic): Boolean
    fun isFrozen(obj: dynamic): Boolean
    fun isSealed(obj: dynamic): Boolean
    fun keys(obj: dynamic): Array<String>
    fun setPrototypeOf(obj: dynamic, proto: dynamic): dynamic
    fun values(obj: dynamic): Array<dynamic>
    fun seal(obj: dynamic): dynamic
}

/**
 * 模拟使用new关键字创建对象
 * @param constructor 对象/构造函数名称
 * @param args 参数
 */
fun JSObject.New(constructor: dynamic, vararg args: dynamic): dynamic {
    if (Typeof(constructor) != "function") {
        throw RuntimeException("First parameter is not a constructor function")
    }

    if (constructor.prototype === undefined || constructor.prototype === null) {
        throw RuntimeException("First parameter is not a constructor; Because constructor.prototype is null or undefined")
    }

    if (Typeof(constructor.prototype) != "object") {
        throw RuntimeException("First parameter is not a constructor;Because constructor.prototype isn't object")
    }
    val obj = create(constructor.prototype)
    constructor.apply(obj, args)
    return obj
}


fun JSObject.Typeof(obj: dynamic): String {
    return js("typeof obj") as String
}

fun JSObject.isObject(obj: dynamic): Boolean {
    return js("Object.prototype.toString.call(obj) === '[object Object]'") as Boolean
}

fun JSObject.stringToJsObject(str: String, reviver: ((key: String, value: Any?) -> Any?)? = null): dynamic {
    return if (reviver == null) JSON.parse(str) else JSON.parse(str, reviver)
}

fun JSObject.jsonObjectToJsObject(obj: JsonObject, reviver: ((key: String, value: Any?) -> Any?)? = null): dynamic {
    return if (reviver == null) JSON.parse(obj.toString()) else JSON.parse(obj.toString(), reviver)
}

fun JSObject.jsonArrayToJsObject(obj: JsonArray, reviver: ((key: String, value: Any?) -> Any?)? = null): dynamic {
    return if (reviver == null) JSON.parse(obj.toString()) else JSON.parse(obj.toString(), reviver)
}

fun JSObject.toJsonString(obj: dynamic): String {
    return JSON.stringify(obj)
}

fun JSObject.toJsonObject(obj: dynamic): JsonObject {
    return Json.parseToJsonElement(JSON.stringify(obj)).jsonObject
}

fun JSObject.toJsonArray(obj: dynamic): JsonArray {
    return Json.parseToJsonElement(JSON.stringify(obj)).jsonArray
}

fun JSObject.keys(obj: dynamic): Array<String> {
    return js("Object.keys(obj)") as Array<String>
}

fun JSObject.values(obj: dynamic): Array<dynamic> {
    return js("Object.values(obj)") as Array<dynamic>
}

fun JSObject.entries(obj: dynamic): Array<Array<dynamic>> {
    return js("Object.entries(obj)") as Array<Array<dynamic>>
}

fun JSObject.has(obj: dynamic, key: String): Boolean {
    return js("obj.hasOwnProperty(key)") as Boolean
}

fun JSObject.get(obj: dynamic, key: String): dynamic {
    return js("obj[key]")
}

fun JSObject.set(obj: dynamic, key: String, value: dynamic) {
    js("obj[key] = value")
}

fun JSObject.remove(obj: dynamic, key: String) {
    js("delete obj[key]")
}

fun JSObject.clear(obj: dynamic) {
    js("obj = {}")
}

fun JSObject.size(obj: dynamic): Int {
    return js("Object.keys(obj).length") as Int
}

fun JSObject.isEmpty(obj: dynamic): Boolean {
    return js("Object.keys(obj).length === 0") as Boolean
}

fun JSObject.forEach(obj: dynamic, callback: (key: String, value: dynamic) -> Unit) {
    js(
        """
            Object.keys(obj).forEach(function(key) {
                callback(key, obj[key])
            })
        """
    )
}

fun JSObject.forEachEntry(obj: dynamic, callback: (key: String, value: dynamic) -> Unit) {
    js(
        """
            Object.entries(obj).forEach(function(entry) {
                callback(entry[0], entry[1])
            })
        """
    )
}

fun JSObject.forEachValue(obj: dynamic, callback: (value: dynamic) -> Unit) {
    js(
        """
            Object.values(obj).forEach(function(value) {
                callback(value)
            })
        """
    )
}

fun JSObject.forEachKey(obj: dynamic, callback: (key: String) -> Unit) {
    js(
        """
            Object.keys(obj).forEach(function(key) {
                callback(key)
            })
        """
    )
}

object JavaClass {
    val Class = js("java.lang.Class")

    fun getClass(name: String): dynamic {
        return Class.forName(name)
    }

    fun getJavaAdapter(name: String): dynamic {
        return eval(name)
    }
}

@JsName("globalThis")
external val globalThis: dynamic

/**
 * https://rhino.github.io/tutorials/scripting_java/#the-javaadapter-constructor
 */
@JsName("JavaAdapter")
external class JavaAdapter(javaIntfOrClass: dynamic, javascriptObject: Any) {
    constructor(javaIntfOrClass: dynamic, interfaces: Array<dynamic>, javascriptObject: Any)
}

fun JavaAdapterByKotlin(fullyQualifiedName: String, javascriptObject: Any): JavaAdapter {
    return JavaAdapter(JavaClass.getJavaAdapter(fullyQualifiedName), javascriptObject)
}

fun JavaAdapterByKotlin(fullyQualifiedName: String, interfaces: Array<String>, javascriptObject: Any): JavaAdapter {
    return JavaAdapter(JavaClass.getJavaAdapter(fullyQualifiedName),interfaces, javascriptObject)
}