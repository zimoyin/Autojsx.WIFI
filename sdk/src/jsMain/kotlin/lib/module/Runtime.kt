package lib.module

external fun importPackage(val0: dynamic)
external fun importClass(val0: String)

object Packages {
    private val Packages = js("Packages")

    operator fun get(key: String): dynamic {
        return Packages[key]
    }

    fun keys(): Array<String> {
        return js("Object.keys(Packages)") as Array<String>
    }

    fun forEach(callback: (key: String, value: Package) -> Unit) {
        val keys = keys()
        for (key in keys) {
            val value = this[key]
            callback(key, Package(value))
        }
    }

    class Package(val value: dynamic) {
//        fun keys(): Array<String> {
//            return js("Object.keys(value)") as Array<String>
//        }

        fun forEach(callback: (key: String, value: Package) -> Unit) {
            val keys = keys()
            for (key in keys) {
                val value = this[key]
                callback(key, Package(value))
            }
        }

        operator fun get(key: String): dynamic {
            return value[key]
        }
    }
}

@JsName("runtime")
external object Runtime {
    /**
     * 导入的jar必须 JDK <=6
     * @param val0 jar 文件的路径
     */
    fun loadJar(val0: String)

    /**
     * 导入的dex必须 JDK <=8
     * @param val0 dex 文件的路径
     */
    fun loadDex(val0: String)

    /**
     * 动态申请安卓的权限
     * @param val0 权限列表
     */
    fun requestPermissions(val0: Array<String>)
}
