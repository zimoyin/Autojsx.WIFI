package github.zimo.autojsx.pojo

data class RunningListPojo(
    var engineId: Int = 0,
    var sourceName: String = "",
    var source: String = "",
    var engineScriptCwd: String = "",
    //TODO 这应该是个数组之类的
    var engineScriptArgv: String="",
    var isStopped: Boolean = false,
){
    override fun toString(): String {
        return "RunningListPojo(engineId=$engineId, srourceName='$sourceName', engineScriptCwd='$engineScriptCwd', engineScriptArgv='$engineScriptArgv', isStoped=$isStopped)"
    }
}
