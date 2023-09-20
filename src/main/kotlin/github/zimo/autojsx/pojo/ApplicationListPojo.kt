package github.zimo.autojsx.pojo

import java.awt.image.BufferedImage

data class ApplicationListPojo(
    var name: String = "",
    var packageName: String = "",
    var installTime: Long = 0,
    var versionName: String = "",
    var versionCode: Int = 0,
    var icon: String? = null,
    var iconImage: BufferedImage? = null,
)
