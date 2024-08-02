package lib.kotlin

import lib.module.open

/**
 * 加载并执行 JS 文件
 * 请将 JS 文件放置于 resources 下
 */
fun loadJsFile(name: String) {
    val file = open(name, "r")
    val fileContent = file.read()
    file.close()

    try {
        eval(fileContent)
    } catch (e: Exception) {
        throw RuntimeException("Error loading js file: $name", e)
    }
}

/**
 * require函数用于加载模块，返回模块中module.exports的值。
 *
 * 该函数有一个参数用于查找模块位置，可以是相对路径(以'./'或'../'开头)，也可以是绝对路径(以'/'开头)，
 * 还可以是以'http://'或'https://'开头的uri地址，用于加载网络模块，出于安全和加载速度考虑，此方式不建议使用。
 *
 * 当没有以这些开头时，将会视为内置模块，从内置模块目录依次查找，
 * 由于历史原因，在脚本主文件中仍然会先尝试解析成相对路径解析，若解析成功则会忽略内置模块直接加载，
 * 强烈不建议使用此方式加载相对路径的模块，该方式在模块中不可用并且被弃用，在未来版本可能会被移除。
 *
 * 和nodejs类似，当传入的是一个目录，则会尝试加载该目录下的index.js文件，
 * 若存在package.json文件则会先解析该文件中的main字段，若main字段指向一个有效的模块将直接加载该模块。
 *
 * 注意：
 * 使用  @JsExport 可以导出
 * 使用  @JsModule 声明模块
 */
external fun require(path: String): dynamic