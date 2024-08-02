package lib.module

import kotlin.js.Json

/**
 *
 * @author : zimo
 * @date : 2024/07/31
 */
@JsName("paddle")
external interface Paddle {
    /**
     * 使用自定义模型进行文字识别
     * @img {Image} 图片
     * @path {String} 自定义模型路径,必须是绝对路径
     * @return {Array}
     *
     */
    fun ocr(img: Image, path: String): Array<String>

    /**
     * 高精度识别，返回值包含坐标，置信度
     * @img {Image} 图片
     * @cpuThreadNum {Number} 识别使用的 CPU 核心数量
     * @useSlim {Boolean} 加载的模型,可选值:
     *      true ocr_v2_for_cpu(slim) :快速模型,默认
     *      false ocr_v2_for_cpu : 精准模型
     * @return {Array}
     * 返回示例:  [{
     *     "bounds": {
     *         "bottom": 535,
     *         "left": 348,
     *         "right": 631,
     *         "top": 384
     *     },
     *     "confidence": 0.9808736,
     *     "inferenceTime": 188.0,
     *     "preprocessTime": 53.0,
     *     "text": "约定",
     *     "words": "约定"
     *  }]
     *
     */
    fun ocr(img: Image, cpuThreadNum: Int, useSlim: Boolean): Array<String>

    /**
     * 只返回文本识别信息
     * @img {Image} 图片
     * @cpuThreadNum {Number} 识别使用的 CPU 核心数量
     * @useSlim {Boolean} 加载的模型,可选值:
     *      true ocr_v2_for_cpu(slim) :快速模型,默认
     *      false ocr_v2_for_cpu : 精准模型
     * @return {Array} 字符串数组
     */
    fun ocrText(img: Image, cpuThreadNum: Int, useSlim: Boolean): Array<String>

    /**
     * 释放 native 内存，非必要，供万一出现内存泄露时使用
     */
    fun release()
}

@JsName("gmlkit")
external interface GoogleOCR {
    /**
     * Google ML kIT OCR
     * @img {Image} 图片
     * @Language {String} 识别语言，可选值为：
     *      la 拉丁
     *      zh 中文
     *      sa 梵文
     *      ja 日语
     *      ko 韩语
     *      更多语言 @see https://developers.google.cn/ml-kit/vision/text-recognition/v2/languages
     * retrun {Object} Json
     */
    fun ocr(img: Image, Language: String): Json
}