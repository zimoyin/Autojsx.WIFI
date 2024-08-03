package lib.kotlin

import kotlinx.serialization.json.*

/**
 *
 * @author : zimo
 * @date : 2024/07/31
 */


fun kotlin.js.Json.toJsonObject(): JsonObject {
    return Json.parseToJsonElement(this.toString()).jsonObject
}

fun kotlin.js.Json.toJsonArray(): JsonArray {
    return Json.parseToJsonElement(this.toString()).jsonArray
}

fun kotlin.js.Json.toJsonString(): String {
    val jsonObj = this
    return js("JSON.stringify(jsonObj)") as String
}

fun JsonObject.toJsonByJs(): kotlin.js.Json {
    val str = this.toString()
    return js("JSON.parse(JSON.stringify(str))") as kotlin.js.Json
}
fun JsonArray.toJsonByJs(): kotlin.js.Json {
    val str = this.toString()
    return js("JSON.parse(JSON.stringify(str))") as kotlin.js.Json
}

fun Json.toJsonByJs(): kotlin.js.Json {
    val str = this.toString()
    return js("JSON.parse(JSON.stringify(str))") as kotlin.js.Json
}