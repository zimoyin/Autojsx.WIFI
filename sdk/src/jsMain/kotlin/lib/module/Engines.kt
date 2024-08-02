package lib.module
@JsName("engines")

external object Engines {

    /**
     * 脚本引擎对象。
     */
    interface ScriptEngine {

        /**
         * 停止脚本引擎的执行。
         */
        fun forceStop()

        /**
         * 返回脚本执行的路径。对于一个脚本文件而言为这个脚本所在的文件夹；对于其他脚本，例如字符串脚本，则为null或者执行时的设置值。
         */
        fun cwd(): String

        /**
         * 检测该脚本是否执行结束
         */
        fun isDestroyed(): Boolean

        /**
         * 返回当前脚本引擎正在执行的脚本对象。
         */
        fun getSource(): String

        /**
         * 向该脚本引擎发送一个事件，该事件可以在该脚本引擎对应的脚本的 events 模块监听到并在脚本主线程执行事件处理。
         * @param eventName 事件名称
         * @param args 事件参数
         */
        fun emit(eventName: String, vararg args: Any)

        fun getThread(): String
        fun getExecArgv(): String

        /**
         * 脚本是否停止运行
         */
        fun destroyed(): Boolean
        val execArgv: String
        val source: String
        val id: String
    }

    /**
     * 执行脚本时返回的对象，可以通过他获取执行的引擎、配置等，也可以停止这个执行。
     *
     * 要停止这个脚本的执行，使用exectuion.getEngine().forceStop().
     */
    interface ScriptExecution {

        /**
         * 返回执行该脚本的脚本引擎对象(ScriptEngine)
         */
        fun getEngine(): ScriptEngine

        /**
         * 返回该脚本的运行配置(ScriptConfig)
         */
        fun getConfig(): ScriptConfig
    }

    /**
     * 运行配置项。
     */
    interface ScriptConfig {

        /**
         * 延迟执行的毫秒数，默认为0。
         */
        var delay: Int?

        /**
         * 循环运行次数，默认为1。0为无限循环。
         */
        var loopTimes: Int?

        /**
         * 循环运行时两次运行之间的时间间隔，默认为0。
         */
        var interval: Int?

        /**
         * 指定脚本运行的目录。这些路径会用于require时寻找模块文件。
         */
        var path: dynamic /* String | Array<String> */

        /**
         * 返回一个字符串数组表示脚本运行时模块寻找的路径。
         */
        var getpath: Array<String>?
    }

    /**
     * 在新的脚本环境中运行脚本script。返回一个ScriptExectuion对象。
     *
     * 所谓新的脚本环境，指定是，脚本中的变量和原脚本的变量是不共享的，并且，脚本会在新的线程中运行。
     */
    fun execScript(name: String, script: String, config: ScriptConfig? = definedExternally): ScriptExecution

    /**
     * 在新的脚本环境中运行脚本文件path:string。返回一个ScriptExecution对象。
     */
    fun execScriptFile(path: String, config: ScriptConfig? = definedExternally): ScriptExecution

    /**
     * 在新的脚本环境中运行录制文件path:string。返回一个ScriptExecution对象。
     */
    fun execAutoFile(path: String, config: ScriptConfig? = definedExternally): ScriptExecution

    /**
     * 停止所有正在运行的脚本。包括当前脚本自身。
     */
    fun stopAll()

    /**
     * 停止所有正在运行的脚本并显示停止的脚本数量。包括当前脚本自身。
     */
    fun stopAllAndToast()

    /**
     * 返回当前脚本的脚本引擎对象(ScriptEngine)
     */
    fun myEngine(): ScriptEngine

    /**
     * 返回当前所有正在运行的脚本的脚本引擎ScriptEngine的数组。
     */
    fun all(): Array<ScriptEngine>
}
