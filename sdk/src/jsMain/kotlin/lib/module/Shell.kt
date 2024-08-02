package lib.module

external interface Result {
    /**
     * 返回码。执行成功时为0，失败时为非0的数字。
     */
    val code: Number

    /**
     * 运行结果(stdout输出结果)
     */
    val result: String

    /**
     * 运行的错误信息(stderr输出结果)。例如执行需要root权限的命令但没有授予root权限会返回错误信息"Permission denied"。
     */
    val error: String
}

/**
 * 一次性执行命令cmd, 并返回命令的执行结果。返回对象的其属性如下:
 * @param cmd 执行的命令
 * @param root 是否以root权限运行，默认为false。
 */
external fun shell(cmd: String, root: Boolean): Result

/**
 * Shell 命令对象
 * 命令参考文档：https://developer.android.com/studio/command-line/adb?hl=zh-cn#shellcommands
 */
external class Shell {
    /**
     * 是否以root权限运行一个shell进程，默认为false。
     * 这将会影响其后使用该Shell对象执行的命令的权限
     */
    constructor(root: Boolean)

    /**
     * 执行命令cmd。命令执行是"异步"的、非阻塞的。也就是不会等待命令完成后才继续向下执行。
     * 使用 execAndWaitFor 来等待执行完毕
     * @param cmd 要执行的命令
     */
    fun exec(cmd: String): Unit

    /**
     * 执行"exit"命令并等待执行命令执行完成、退出shell。
     * 此函数会执行exit命令来正常退出shell。
     */
    fun exitAndWaitFor(): Unit

    /**
     * 设置该Shell的回调函数，以便监听Shell的输出。
     * @param callback
     */
    fun setCallback(callback: Callback): Unit

    interface Callback {
        fun onNewLine(line: String)
        fun onOutput(output: String)
    }
}
