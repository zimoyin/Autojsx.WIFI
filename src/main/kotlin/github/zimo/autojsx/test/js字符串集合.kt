package github.zimo.autojsx.test

import java.io.File

/**
 * js 字符串放入数组中
 * TODO 将 输出转为程序中的控制台输出
 */
fun main() {
    // 读取 JavaScript 文件的内容
    val jsFile = File("jsfile.js")
    var jsCode = jsFile.readText()

    // 从 JavaScript 代码中提取字符串并放入集合
    val arrayStrings = mutableListOf<String>()
    val map: HashMap<IntRange, String> = HashMap()
    val arrayName = "_zimo_StringArray_idea_"

    println("注意: 禁止字符串嵌套，如：双引号出现在单引号内")
    // 将双引号内容放入数组
    while (true) {
        val regex = Regex("\"(.*?)\"") // 正则表达式匹配引号之间的字符串
        regex.find(jsCode)?.let {
            val string = it.groupValues[1]
            map[it.range] = string
            arrayStrings.add(string)
            var isChange = true
            if (isOddNumber(
                    jsCode,
                    it.range.first, '\''
                )
            ) {
                println("该字符串前发现未闭合的单引号,这可能会出现一些影响。字符串: $string 字符索引: ${it.range.first}")
            }
            if (isOddNumber(
                    jsCode,
                    it.range.first, '\"'
                )
            ) {
                println("该字符串前发现未闭合的双引号,这可能会出现一些影响。字符串: $string 字符索引: ${it.range.first}")
            }
            jsCode = jsCode.substring(
                0,
                it.range.first
            ) + "$arrayName[${arrayStrings.size - 1}]" + jsCode.substring(it.range.last + 1)
        } ?: break
    }
    // 将单引号内容放入数组
    while (true) {
        val regex = Regex("\'(.*?)\'") // 正则表达式匹配引号之间的字符串
        regex.find(jsCode)?.let {
            var string = it.groupValues[1]
            map[it.range] = string
            if (isOddNumber(
                    jsCode,
                    it.range.first, '\"'
                )
            ) println("该字符串前发现未闭合的双引号,这可能会出现一些影响。字符串: $string 字符串开始索引位置: ${it.range.first}")
            // 修正错误嵌套关系: 单引号内的双引号被解析了
            string = errorCorrection(string, arrayName, arrayStrings)

            arrayStrings.add(string)

            jsCode = jsCode.substring(
                0,
                it.range.first
            ) + "$arrayName[${arrayStrings.size - 1}]" + jsCode.substring(it.range.last + 1)
        } ?: break
    }


    // 构建集合
    val jsStringBuilder = StringBuilder()
    jsStringBuilder.append("const $arrayName = [")
    arrayStrings.forEachIndexed { index, string ->
        jsStringBuilder.append("\"").append(string.toUnicode()).append("\"")
        if (index < arrayStrings.size - 1) {
            jsStringBuilder.append(", ")
        }
    }
    jsStringBuilder.append("];")

    // 将生成的 JavaScript 代码写入新的文件
    val outputFile = File("outputfile.js")
    outputFile.writeText(jsStringBuilder.toString() + "\n" + jsCode)


    println(jsStringBuilder.toString() + "\n" + jsCode)
}

/**
 * 修正错误嵌套关系: 单引号内的双引号被解析了
 */
private fun errorCorrection(
    string: String,
    arrayName: String,
    arrayStrings: MutableList<String>,
): String {
    var string1 = string
    if (string1.contains(arrayName)) {
        println("检测到了错误嵌套: 单引号内的双引号被解析了。尝试修正")
        while (true) {
            if (string1.contains(arrayName)) {
                //错误位置
                try {
                    val errorIndexStart = string1.indexOf("$arrayName[")
                    val errorIndexEnd = string1.indexOf("]") + 1
                    val contentIndex =
                        string1.substring(errorIndexStart + "$arrayName[".length, errorIndexEnd - 1) // 这个内容索引
                    string1 = string1.substring(
                        0,
                        errorIndexStart
                    ) + "\"${arrayStrings[contentIndex.toInt()]}\"" + string1.substring(errorIndexEnd)
                } catch (e: Exception) {
                    println("修正失败")
                }
            } else break
        }
    }
    return string1
}

/**
 * 将字符串转为 Unicode 码
 */
fun String.toUnicode(): String {
    val builder = StringBuilder()
    for (char in this) {
        builder.append("\\u").append(String.format("%04x", char.toInt()))
    }
    return builder.toString()
}


/**
 * 该字符串出现的个数
 */
fun numberOfCharacters(input: String, position: Int, symbol: Char): Int {
    var singleQuoteCount = 0
    for (i in 0 until position) {
        if (input[i] == symbol) {
            singleQuoteCount++
        }
    }
    return singleQuoteCount
}

/**
 * 该字符串是否是奇数个
 */
fun isOddNumber(input: String, position: Int, symbol: Char): Boolean {
    val singleQuoteCount = numberOfCharacters(input, position, symbol)
    return singleQuoteCount % 2 != 0
}