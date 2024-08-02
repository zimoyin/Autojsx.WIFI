package lib.module

@JsName("http")
external object Http {

    /**
     * HTTP 请求选项接口
     */
    interface HttpRequestOptions {
        var header: dynamic // { [key: string]: string }
        var method: Method
        var contentType: String
        var body: dynamic // String | String[] | files.byte[]
    }

    /**
     * 请求对象接口（暂未定义具体方法）
     */
    interface Request

    /**
     * 响应对象接口
     */
    interface Response {
        var statusCode: Number
        var statusMessage: String
        var headers: dynamic // { [key: string]: string }
        var body: ResponseBody
        var request: Request
        var url: String
        var method: Method
    }

    /**
     * 响应体接口
     */
    interface ResponseBody {
        fun bytes(): Array<Byte>
        fun string(): String
        fun json(): dynamic // Object
        var contentType: String
    }

    /**
     * HTTP 方法枚举
     */
    enum class Method {
        GET,
        POST,
        PUT,
        DELETE, // Fixed typo from 'DELET' to 'DELETE'
        PATCH
    }

    /**
     * 发送 GET 请求
     * @param url 请求的 URL
     * @param options 请求选项
     * @param callback 响应处理回调
     * @return 响应对象
     */
    fun get(url: String, options: HttpRequestOptions? = definedExternally, callback: ((Response) -> Unit)? = definedExternally): Response

    /**
     * 发送 POST 请求
     * @param url 请求的 URL
     * @param data 请求数据
     * @param options 请求选项
     * @param callback 响应处理回调
     * @return 响应对象
     */
    fun post(url: String, data: dynamic, options: HttpRequestOptions? = definedExternally, callback: ((Response) -> Unit)? = definedExternally): Response

    /**
     * 发送 JSON 数据的 POST 请求
     * @param url 请求的 URL
     * @param data 请求数据
     * @param options 请求选项
     * @param callback 响应处理回调
     * @return 响应对象
     */
    fun postJson(url: String, data: dynamic? = definedExternally, options: HttpRequestOptions? = definedExternally, callback: ((Response) -> Unit)? = definedExternally): Response

    /**
     * 请求体文件接口
     */
    interface RequestMultipartBody {
        var file: dynamic // ReadableTextFile | [String, String] | [String, String, String]
    }

    /**
     * 发送 multipart 请求
     * @param url 请求的 URL
     * @param files 请求文件
     * @param options 请求选项
     * @param callback 响应处理回调
     */
    fun postMultipart(url: String, files: dynamic, options: HttpRequestOptions? = definedExternally, callback: ((Response) -> Unit)? = definedExternally)

    /**
     * 发送请求
     * @param url 请求的 URL
     * @param options 请求选项
     * @param callback 响应处理回调
     */
    fun request(url: String, options: HttpRequestOptions? = definedExternally, callback: ((Response) -> Unit)? = definedExternally)
}
