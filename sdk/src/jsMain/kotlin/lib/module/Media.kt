package lib.module

@JsName("media")

external object Media {

    /**
     * 扫描文件以添加到媒体库中
     * @param path 文件路径
     */
    fun scanFile(path: String): Unit

    /**
     * 播放音乐
     * @param path 音乐文件路径
     * @param volume 音量 (可选)
     * @param looping 是否循环播放 (可选)
     */
    fun playMusic(path: String, volume: Double? = definedExternally, looping: Boolean? = definedExternally): Unit

    /**
     * 跳转到音乐的指定位置
     * @param msec 毫秒位置
     */
    fun musicSeekTo(msec: Int): Unit

    /**
     * 暂停音乐
     */
    fun pauseMusic(): Unit

    /**
     * 恢复音乐播放
     */
    fun resumeMusic(): Unit

    /**
     * 停止音乐
     */
    fun stopMusic(): Unit

    /**
     * 检查音乐是否正在播放
     * @return 是否正在播放
     */
    fun isMusicPlaying(): Boolean

    /**
     * 获取音乐总时长
     * @return 音乐时长（毫秒）
     */
    fun getMusicDuration(): Int

    /**
     * 获取音乐当前播放位置
     * @return 当前播放位置（毫秒）
     */
    fun getMusicCurrentPosition(): Int
}
