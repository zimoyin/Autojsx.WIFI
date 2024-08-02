package lib.module

import lib.kotlin.JSObject
import lib.kotlin.New
import lib.kotlin.require
import kotlin.js.Promise
import kotlin.js.json

/**
 *
 * @author : zimo
 * @date : 2024/07/31
 */
object Axios {
    val axios_js: dynamic = require("axios")

    /**
     * 设置 Axios 返回值类型
     * 示例
     *     Axios.axios("https://baidu.com",Axios.responseType("blob")).then {
     *         println(it.data)
     *     }
     */
    fun responseType(responseType: String = "text"): kotlin.js.Json {
        return json("responseType" to responseType)
    }

    /**
     * axios请求
     * @data 请求数据。支持以下参数类型
     *  responseType 指定返回值类型
     *  RequestBody okhttp3.RequestBody对象
     *  FormData
     *  Blob
     *  InputStream java输入流
     *  String
     *  plain object 会解析成json
     */
    fun axios(url: String, data: dynamic = null): Promise<AxiosResponse> {
        return if (data == null) axios_js(url) as Promise<AxiosResponse> else axios_js(
            url,
            data
        ) as Promise<AxiosResponse>
    }

    fun get(url: String, data: dynamic = null): Promise<AxiosResponse> {
        return if (data == null) axios_js.get(url) as Promise<AxiosResponse> else axios_js.get(
            url,
            data
        ) as Promise<AxiosResponse>
    }

    fun post(url: String, data: dynamic = null): Promise<AxiosResponse> {
        return if (data == null) axios_js.post(url) as Promise<AxiosResponse> else axios_js.post(
            url,
            data
        ) as Promise<AxiosResponse>
    }

    object Utils {

        /**
         * 打开一个文件，返回一个blob对象
         */
        fun openFile(file: String): dynamic {
            return axios_js.utils.openFile(file)
        }

        /**
         * 保存blob对象到指定路径，返回一个Promise。
         * blob {Blob} 要保存的对象
         * path {String} 保存路径
         */
        fun saveBlobToFile(blob: dynamic, path: String): Promise<dynamic> {
            return axios_js.utils.saveBlobToFile(blob, path) as Promise<dynamic>
        }

        /**
         * 拷贝输入流到输出流，这个函数是阻塞的，且不会自动关闭流。
         * inputstreamjava输入流
         * outputstream java输出流
         */
        fun copyInputStream(inputstream: dynamic, outputstream: dynamic) {
            axios_js.utils.copyInputStream(inputstream, outputstream)
        }

        /**
         * 此对象用于将一个同步函数转成异步方法运行，返回一个Promise，例如
         *
         * let promise = ThreadPool.run(()>{
         *   //同步代码，返回值就是Promise的返回值
         * })
         */
        object ThreadPool {
            fun run(func: () -> dynamic): Promise<dynamic> {
                return axios_js.utils.ThreadPool.run(func) as Promise<dynamic>
            }
        }
    }
}

/**
 * FormData 表单
 * 由于该对象依赖于 Axios，所以需要先初始化 Axios 才能正常导入
 * 请使用 FormDataFactory 创建 FormData 对象
 */
class FormData {

    val obj: dynamic
        get() {
            val axios = Axios.axios_js
            val formData_js: dynamic = axios.browser.FormData
            return JSObject.New(formData_js)
        }

    fun append(key: String, value: dynamic) {
        obj.append(key, value)
    }

    fun set(key: String, value: dynamic) {
        obj.set(key, value)
    }

    fun get(key: String): dynamic {
        return obj.get(key)
    }
}

external interface AxiosResponse {
    /**
     * 支持的responseType:
     *      text
     *      json
     *      blob
     *      inputstream java输入流
     *      stream Readable可读流 *v6.4.0新增
     */
    val data: dynamic
    val status: Int
    val statusText: String
    val headers: dynamic
    val config: dynamic
    val request: dynamic
}