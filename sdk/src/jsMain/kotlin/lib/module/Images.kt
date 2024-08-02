package lib.module

import org.w3c.dom.Image

@JsName("images")

external object Images {

    /**
     * images.matToImage(mat)
     * [v4_1_0新增]
     *
     * mat {Mat} OpenCV的Mat对象
     * 返回 {Image}
     * 把Mat对象转换为Image对象。
     */
    fun matToImage(mat: dynamic): Image

    /**
     * images.gaussianBlur(img, size[, sigmaX, sigmaY, type])
     * [v4_1_0新增]
     *
     * img {Image} 图片
     * size {Array} 定义滤波器的大小，如[3, 3]
     * sigmaX {number} x方向的标准方差，不填写则自动计算
     * sigmaY {number} y方向的标准方差，不填写则自动计算
     * type {string} 推断边缘像素类型，默认为"DEFAULT"，参见images.blur
     * 返回 {Image}
     * 对图像进行高斯模糊，返回处理后的图像。
     *
     * 可以参考有关博客（比如实现图像平滑处理）或者OpenCV文档GaussianBlur。
     */
    fun gaussianBlur(image: Image, size: Array<Int>, sigmaX: Int, sigmaY: Int, type: String): Image

    /**
     * images.medianBlur(img, size)
     * [v4_1_0新增]
     *
     * img {Image} 图片
     * size {Array} 定义滤波器的大小，如[3, 3]
     * 返回 {Image}
     * 对图像进行中值滤波，返回处理后的图像。
     *
     * 可以参考有关博客（比如实现图像平滑处理）或者OpenCV文档blur。
     */
    fun medianBlur(image: Image, size: Array<Int>): Image

    /**
     * images.blur(img, size[, anchor, type])
     * [v4_1_0新增]
     *
     * img {Image} 图片
     * size {Array} 定义滤波器的大小，如[3, 3]
     * anchor {Array} 指定锚点位置(被平滑点)，默认为图像中心
     * type {string} 推断边缘像素类型，默认为"DEFAULT"，可选的值有：
     * CONSTANT iiiiii|abcdefgh|iiiiiii with some specified i
     * REPLICATE aaaaaa|abcdefgh|hhhhhhh
     * REFLECT fedcba|abcdefgh|hgfedcb
     * WRAP cdefgh|abcdefgh|abcdefg
     * REFLECT_101 gfedcb|abcdefgh|gfedcba
     * TRANSPARENT uvwxyz|abcdefgh|ijklmno
     * REFLECT101 same as BORDER_REFLECT_101
     * DEFAULT same as BORDER_REFLECT_101
     * ISOLATED do not look outside of ROI
     * 返回 {Image}
     * 对图像进行模糊（平滑处理），返回处理后的图像。
     *
     * 可以参考有关博客（比如实现图像平滑处理）或者OpenCV文档blur。
     */
    fun blur(image: Image, size: Array<Int>, anchor: Array<Int>, type: String): Image
    fun blur(image: Image, size: Array<Int>, anchor: Array<Int>): Image

    /**
     * images.interval(img, color, interval)
     * [v4_1_0新增]
     *
     * img {Image} 图片
     * color {string} | {number} 颜色值
     * interval {number} 每个通道的范围间隔
     * 返回 {Image}
     * 将图片二值化，在color-interval ~ color+interval范围以外的颜色都变成0，在范围以内的颜色都变成255。这里对color的加减是对每个通道而言的。
     *
     * 例如images.interval(img, "#888888", 16)，每个通道的颜色值均为0x88，加减16后的范围是[0x78, 0x98]，因此这个代码将把#787878~#989898的颜色变成#FFFFFF，而把这个范围以外的变成#000000。
     */
    fun interval(image: Image, color: String, interval: Int): Image

    /**
     * images.inRange(img, lowerBound, upperBound)
     * [v4_1_0新增]
     *
     * img {Image} 图片
     * lowerBound {string} | {number} 颜色下界
     * upperBound {string} | {number} 颜色下界
     * 返回 {Image}
     * 将图片二值化，在lowerBound~upperBound范围以外的颜色都变成0，在范围以内的颜色都变成255。
     *
     * 例如images.inRange(img, "#000000", "#222222")。
     */
    fun inRange(image: Image, lowerBound: String, upperBound: String): Image

    /**
     * images.cvtColor(img, code[, dstCn])
     * [v4_1_0新增]
     *
     * img {Image} 图片
     * code {string} 颜色空间转换的类型，可选的值有一共有205个（参见ColorConversionCodes），这里只列出几个：
     * BGR2GRAY BGR转换为灰度
     * BGR2HSV  BGR转换为HSV
     * ``
     * dstCn {number} 目标图像的颜色通道数量，如果不填写则根据其他参数自动决定。
     * 返回 {Image}
     * 对图像进行颜色空间转换，并返回转换后的图像。
     *
     * 可以参考有关博客（比如颜色空间转换）或者OpenCV文档cvtColor。
     */
    fun cvtColor(image: Image, code: String, dstCn: Int? = definedExternally): Image
    fun cvtColor(image: Image, code: String): Image

    /**
     * images.adaptiveThreshold(img, maxValue, adaptiveMethod, thresholdType, blockSize, C)
     * [v4_1_0新增]
     *
     * img {Image} 图片
     * maxValue {number} 最大值
     * adaptiveMethod {string} 在一个邻域内计算阈值所采用的算法，可选的值有：
     * MEAN_C 计算出领域的平均值再减去参数C的值
     * GAUSSIAN_C 计算出领域的高斯均值再减去参数C的值
     * thresholdType {string} 阈值化类型，可选的值有：
     * BINARY
     * BINARY_INV
     * blockSize {number} 邻域块大小
     * C {number} 偏移值调整量
     * 返回 {Image}
     * 对图片进行自适应阈值化处理，并返回处理后的图像。
     *
     * 可以参考有关博客（比如threshold与adaptiveThreshold）或者OpenCV文档adaptiveThreshold。
     */
    fun adaptiveThreshold(
        image: Image,
        maxValue: Int,
        adaptiveMethod: String,
        thresholdType: String,
        blockSize: Int,
        C: Int
    ): Image

    /**
     * image.threshold(img, threshold, maxVal[, type])
     * [v4_1_0新增]
     *
     * img {Image} 图片
     * threshold {number} 阈值
     * maxVal {number} 最大值
     * type {string} 阈值化类型，默认为"BINARY"，参见ThresholdTypes, 可选的值:
     * BINARY
     * BINARY_INV
     * TRUNC
     * TOZERO
     * TOZERO_INV
     * OTSU
     * TRIANGLE
     * 返回 {Image}
     * 将图片阈值化，并返回处理后的图像。可以用这个函数进行图片二值化。例如：images.threshold(img, 100, 255, "BINARY")，这个代码将图片中大于100的值全部变成255，其余变成0，从而达到二值化的效果。如果img是一张灰度化图片，这个代码将会得到一张黑白图片。
     *
     * 可以参考有关博客（比如threshold函数的使用）或者OpenCV文档threshold。
     */
    fun threshold(image: Image, threshold: Int, maxVal: Int, type: String): Image
    fun threshold(image: Image, threshold: Int, maxVal: Int): Image

    /**
     * images.grayscale(img)
     * [v4_1_0新增]
     *
     * img {Image} 图片
     * 返回 {Image}
     * 灰度化图片，并返回灰度化后的图片。
     */
    fun grayscale(image: Image): Image

    /**
     * images.concat(img1, image2[, direction])
     * [v4_1_0新增]
     *
     * img1 {Image} 图片1
     * img2 {Image} 图片2
     * direction {string} 连接方向，默认为"RIGHT"，可选的值有：
     * LEFT 将图片2接到图片1左边
     * RIGHT 将图片2接到图片1右边
     * TOP 将图片2接到图片1上边
     * BOTTOM 将图片2接到图片1下边
     * 返回 {Image}
     * 连接两张图片，并返回连接后的图像。如果两张图片大小不一致，小的那张将适当居中。
     */
    fun concat(image1: Image, image2: Image, direction: String): Image
    fun concat(image1: Image, image2: Image): Image


    /**
     * images.rotate(img, degress[, x, y])
     * [v4_1_0新增]
     *
     * img {Image} 图片
     * degress {number} 旋转角度。
     * x {number} 旋转中心x坐标，默认为图片中点
     * y {number} 旋转中心y坐标，默认为图片中点
     * 返回 {Image}
     * 将图片逆时针旋转degress度，返回旋转后的图片对象。
     *
     * 例如逆时针旋转90度为images.rotate(img, 90)。
     */
    fun rotate(image: Image, degrees: Int, x: Int? = definedExternally, y: Int? = definedExternally): Image
    fun rotate(image: Image, degrees: Int): Image

    /**
     * images.scale(img, fx, fy[, interpolation])
     * [v4_1_0新增]
     *
     * img {Image} 图片
     *
     * fx {number} 宽度放缩倍数
     *
     * fy {number} 高度放缩倍数
     *
     * interpolation {string} 插值方法，可选，默认为"LINEAR"（线性插值），可选的值有：
     *
     * NEAREST 最近邻插值
     * LINEAR 线性插值（默认）
     * AREA 区域插值
     * CUBIC 三次样条插值
     * LANCZOS4 Lanczos插值 参见InterpolationFlags
     * 返回 {Image}
     *
     * 放缩图片，并返回放缩后的图片。例如把图片变成原来的一半：images.scale(img, 0.5, 0.5)。
     *
     * 参见 Imgproc.resize。
     */
    fun scale(image: Image, fx: Int, fy: Int, interpolation: String): Image
    fun scale(image: Image, fx: Int, fy: Int): Image

    /**
     * images.resize(img, size[, interpolation])
     * [v4_1_0新增]
     *
     * img {Image} 图片
     *
     * size {Array} 两个元素的数组[w, h]，分别表示宽度和高度；如果只有一个元素，则宽度和高度相等
     *
     * interpolation {string} 插值方法，可选，默认为"LINEAR"（线性插值），可选的值有：
     *
     * NEAREST 最近邻插值
     * LINEAR 线性插值（默认）
     * AREA 区域插值
     * CUBIC 三次样条插值
     * LANCZOS4 Lanczos插值 参见InterpolationFlags
     * 返回 {Image}
     *
     * 调整图片大小，并返回调整后的图片。例如把图片放缩为200*300：images.resize(img, [200, 300])。
     */
    fun resize(image: Image, size: Array<Int>, interpolation: String): Image
    fun resize(image: Image, size: Array<Int>): Image

    /**
     * 把图片image以PNG格式保存到path中。如果文件不存在会被创建；文件存在会被覆盖。
     * @image {Image} 图片
     * @path {string} 路径
     * @format {string} 图片格式，可选的值为:
     *      png
     *      jpeg/jpg
     *      webp
     * @quality {number} 图片质量，为0~100的整数值
     *
     */
    fun save(image: Image, path: String, format: String, quality: Int)
    fun save(image: Image, path: String)

    /**
     * 复制一张图片并返回新的副本。该函数会完全复制img对象的数据。
     */
    fun copy(image: Image): Image

    /**
     * 读取在路径path的图片文件并返回一个Image对象。如果文件不存在或者文件无法解码则返回null。
     */
    fun read(path: String): Image

    /**
     * 加载在地址URL的网络图片并返回一个Image对象。如果地址不存在或者图片无法解码则返回null。
     */
    fun load(url: String): Image

    /**
     * 裁剪图片
     * @param image {Image} 图片
     * @param x
     * @param y
     * @param w
     * @param h
     */
    fun clip(image: Image, x: Int, y: Int, w: Int, h: Int): Image

    /**
     * images.toBytes(img[, format = "png", quality = 100])
     * image {image} 图片
     * format {string} 图片格式，可选的值为:
     * png
     * jpeg/jpg
     * webp
     * quality {number} 图片质量，为0~100的整数值
     * 返回 {byte[]}
     * 把图片编码为字节数组并返回。
     */
    fun toBytes(image: Image, format: String, quality: Int): ByteArray
    fun toBytes(image: Image): ByteArray

    /**
     * 解码字节数组bytes并返回解码后的图片Image对象。如果bytes无法解码则返回null。
     * @bytes {byte[]} 字节数组
     */
    fun fromBytes(bytes: ByteArray): Image


    /**
     * 解码Base64数据并返回解码后的图片Image对象。如果base64无法解码则返回null。
     */
    fun fromBase64(base64: String): Image

    /**
     * 把图片编码为base64数据并返回
     * @image {image} 图片
     * @format {string} 图片格式，可选的值为:
     *      png
     *      jpeg/jpg
     *      webp
     * @quality {number} 图片质量，为0~100的整数值
     */
    fun toBase64(img: Image, format: String, quality: Int): String
    fun toBase64(img: Image): String


    ///////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////           找图找色             ///////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 向系统申请屏幕截图权限，返回是否请求成功,仅需执行一次
     * 建议在本软件界面运行该函数，在其他软件界面运行时容易出现一闪而过的黑屏现象。
     * @landscape {boolean} 截屏方向
     *      true 横屏截图
     *      false 竖屏截图
     *      不指定值，由当前设备屏幕方向决定截图方向
     * @return {boolean}
     *
     * 示例1：
     * //请求截图
     * //每次使用该函数都会弹出截图权限请求，建议选择“总是允许”。
     * if(!requestScreenCapture()){
     *     toast("请求截图失败");
     *     exit();
     * }
     * //连续截图 10 张图片(间隔 1 秒)并保存到存储卡目录
     * for(var i = 0; i < 10; i++){
     *     captureScreen("/sdcard/screencapture" + i + ".png");
     *     sleep(1000);
     * }
     *
     * 示例2：
     * //安卓版本高于Android 9
     * if(device.sdkInt>28){
     *     //等待截屏权限申请并同意
     *     threads.start(function () {
     *         packageName('com.android.systemui').text('立即开始').waitFor();
     *         text('立即开始').click();
     *     });
     * }
     * //申请截屏权限
     * if (!requestScreenCapture()) {
     *     toast("请求截图失败");
     *     exit()
     * }
     *
     */
    fun requestScreenCapture(b: Boolean): Boolean

    /**
     * 截图
     * 截取当前屏幕并返回一个Image对象。
     * 没有截图权限时执行该函数会抛出SecurityException。
     * 该函数不会返回null，两次调用可能返回相同的Image对象。这是因为设备截图的更新需要一定的时间，短时间内（一般来说是16ms）连续调用则会返回同一张截图。
     * 截图需要转换为Bitmap格式，从而该函数执行需要一定的时间(0~20ms)。
     * 另外在requestScreenCapture()执行成功后需要一定时间后才有截图可用，因此如果立即调用captureScreen()，会等待一定时间后(一般为几百ms)才返回截图。
     */
    fun captureScreen(): Image

    fun captureScreen(path: String)

    /**
     * @param image {Image} 图片
     * @param x {number} 要获取的像素的横坐标。
     * @param y {number} 要获取的像素的纵坐标。
     * 返回图片image在点(x, y)处的像素的ARGB值。
     *
     * 该值的格式为0xAARRGGBB，是一个"32位整数"(虽然JavaScript中并不区分整数类型和其他数值类型)。
     *
     * 坐标系以图片左上角为原点。以图片左侧边为y轴，上侧边为x轴。
     */
    fun pixel(image: Image, x: Int, y: Int): Int

    /**
     * images.readPixels(path)
     * path {string} 图片的地址
     * return {Object} 包括图片的像素数据和宽高，{data,width,height}
     * 读取图片的像素数据和宽高。
     */
    fun readPixels(path: String): dynamic

    /**
     * images.findColor(image, color, options)
     * image {Image} 图片
     * color {number} | {string} 要寻找的颜色的RGB值。如果是一个整数，则以0xRRGGBB的形式代表RGB值（A通道会被忽略）；如果是字符串，则以"#RRGGBB"代表其RGB值。
     * options {Object} 选项包括：
     * region {Array} 找色区域。是一个两个或四个元素的数组。(region[0], region[1])表示找色区域的左上角；region[2]*region[3]表示找色区域的宽高。如果只有region只有两个元素，则找色区域为(region[0], region[1])到屏幕右下角。如果不指定region选项，则找色区域为整张图片。
     * threshold {number} 找色时颜色相似度的临界值，范围为0255（越小越相似，0为颜色相等，255为任何颜色都能匹配）。默认为4。threshold和浮点数相似度(0.01.0)的换算为 similarity = (255 - threshold) / 255.
     * 在图片中寻找颜色color。找到时返回找到的点Point，找不到时返回null。
     *
     * 该函数也可以作为全局函数使用。
     */
    fun findColor(
        image: Image,
        color: dynamic /* Int | String */,
        options: FindColorOptions? = definedExternally
    ): Point

    /**
     * 区域找色的简便方法。
     */
    fun findColorInRegion(
        image: Image,
        color: dynamic /* Int | String */,
        x: Int,
        y: Int,
        width: Int? = definedExternally,
        height: Int? = definedExternally,
        threshold: Int? = definedExternally
    ): Point

    /**
     * img {Image} 图片
     * color {number} | {string} 要寻找的颜色
     * x {number} 找色区域的左上角横坐标
     * y {number} 找色区域的左上角纵坐标
     * width {number} 找色区域的宽度
     * height {number} 找色区域的高度
     * 返回 {Point}
     * 在图片img指定区域中找到颜色和color完全相等的某个点，并返回该点的左边；如果没有找到，则返回null。
     *
     * 找色区域通过x, y, width, height指定，如果不指定找色区域，则在整张图片中寻找。
     *
     * 该函数也可以作为全局函数使用。
     */
    fun findColorEquals(
        image: Image,
        color: dynamic /* Int | String */,
        x: Int? = definedExternally,
        y: Int? = definedExternally,
        width: Int? = definedExternally,
        height: Int? = definedExternally
    ): Point

    /**
     * 在图片中寻找所有颜色为color的点。找到时返回找到的点 Point 的数组，找不到时返回null。
     * @param img {Image} 图片
     * @param color {number | string} 要检测的颜色
     * @param options {Object} 选项包括：
     * @param region {Array} 找色区域。是一个两个或四个元素的数组。(region[0], region[1])表示找色区域的左上角；region[2]*region[3]表示找色区域的宽高。如果 region只有两个元素，则找色区域为(region[0], region[1])到图片右下角。如果不指定region选项，则找色区域为整张图片。
     * @param similarity {number} 找色时颜色相似度，范围为 0~1（越大越相似，1 为颜色相等，0 为任何颜色都能匹配）。
     * @param threshold {number} 找色时颜色相似度的临界值，范围为 0 ~ 255（越小越相似，0 为颜色相等，255 为任何颜色都能匹配）。默认为 4。
     * @param similarity与threshold的换算为similarity = (255 - threshold) / 255 。二选一，同时存在则以similarity为准。
     * @return {Array}
     */
    fun findAllPointsForColor(
        image: Image,
        color: dynamic /* Int | String */,
        options: FindColorOptions? = definedExternally
    ): Array<Point>

    /**
     * images.findMultiColors(img, firstColor, colors[, options])
     * img {Image} 要找色的图片
     * firstColor {number} | {string} 第一个点的颜色
     * colors {Array} 表示剩下的点相对于第一个点的位置和颜色的数组，数组的每个元素为[x, y, color]
     * options {Object} 选项，包括：
     * region {Array} 找色区域。是一个两个或四个元素的数组。(region[0], region[1])表示找色区域的左上角；region[2]*region[3]表示找色区域的宽高。如果只有region只有两个元素，则找色区域为(region[0], region[1])到屏幕右下角。如果不指定region选项，则找色区域为整张图片。
     * threshold {number} 找色时颜色相似度的临界值，范围为0255（越小越相似，0为颜色相等，255为任何颜色都能匹配）。默认为4。threshold和浮点数相似度(0.01.0)的换算为 similarity = (255 - threshold) / 255.
     * 多点找色，类似于按键精灵的多点找色，其过程如下：
     *
     * 在图片img中找到颜色firstColor的位置(x0, y0)
     * 对于数组colors的每个元素[x, y, color]，检查图片img在位置(x + x0, y + y0)上的像素是否是颜色color，是的话返回(x0, y0)，否则继续寻找firstColor的位置，重新执行第1步
     * 整张图片都找不到时返回null
     * 例如，对于代码images.findMultiColors(img, "#123456", [[10, 20, "#ffffff"], [30, 40, "#000000"]])，假设图片在(100, 200)的位置的颜色为#123456, 这时如果(110, 220)的位置的颜色为#fffff且(130, 240)的位置的颜色为#000000，则函数返回点(100, 200)。
     */
    fun findMultiColors(
        image: Image,
        firstColor: dynamic /* Int | String */,
        colors: Array<dynamic /* [Int, Int, Int] */>,
        options: FindColorOptions? = definedExternally
    ): Point

    /**
     * image {Image} 图片
     * color {number} | {string} 要检测的颜色
     * x {number} 要检测的位置横坐标
     * y {number} 要检测的位置纵坐标
     *
     * threshold {number} 颜色相似度临界值，默认为16。取值范围为0~255。
     * algorithm {string} 颜色匹配算法，包括:
     * "equal": 相等匹配，只有与给定颜色color完全相等时才匹配。
     * "diff": 差值匹配。与给定颜色的R、G、B差的绝对值之和小于threshold时匹配。
     * "rgb": rgb欧拉距离相似度。与给定颜色color的rgb欧拉距离小于等于threshold时匹配。
     * "rgb+": 加权rgb欧拉距离匹配(LAB Delta E)。
     * "hs": hs欧拉距离匹配。hs为HSV空间的色调值。
     *
     * 返回图片image在位置(x, y)处是否匹配到颜色color。用于检测图片中某个位置是否是特定颜色。
     */
    fun detectsColor(
        image: Image,
        color: dynamic /* Int | String */,
        x: Int,
        y: Int,
        threshold: Int? = definedExternally,
        algorithm: String? = definedExternally
    ): Point

    /**
     * img {Image} 大图片
     * template {Image} 小图片（模板）
     * options {Object} 选项包括：
     * threshold {number} 图片相似度。取值范围为0~1的浮点数。默认值为0.9。
     * region {Array} 找图区域。参见findColor函数关于region的说明。
     * level {number} 一般而言不必修改此参数。不加此参数时该参数会根据图片大小自动调整。找图算法是采用图像金字塔进行的, level参数表示金字塔的层次, level越大可能带来越高的找图效率，但也可能造成找图失败（图片因过度缩小而无法分辨）或返回错误位置。因此，除非您清楚该参数的意义并需要进行性能调优，否则不需要用到该参数。
     * 找图。在大图片img中查找小图片template的位置（模块匹配），找到时返回位置坐标(Point)，找不到时返回null。
     *
     * 该函数也可以作为全局函数使用
     */
    fun findImage(image: Image, template: Image, options: FindImageOptions? = definedExternally): Point

    /**
     * 区域找图的简便方法。
     */
    fun findImageInRegion(
        image: Image,
        template: Image,
        x: Int,
        y: Int,
        width: Int? = definedExternally,
        height: Int? = definedExternally,
        threshold: Int? = definedExternally
    ): Point

    /**
     * images.matchTemplate(img, template, options)
     *
     * img {Image} 大图片
     * template {Image} 小图片（模板）
     * options {Object} 找图选项：
     * threshold {number} 图片相似度。取值范围为0~1的浮点数。默认值为0.9。
     * region {Array} 找图区域。参见findColor函数关于region的说明。
     * max {number} 找图结果最大数量，默认为5
     * level {number} 一般而言不必修改此参数。不加此参数时该参数会根据图片大小自动调整。找图算法是采用图像金字塔进行的, level参数表示金字塔的层次, level越大可能带来越高的找图效率，但也可能造成找图失败（图片因过度缩小而无法分辨）或返回错误位置。因此，除非您清楚该参数的意义并需要进行性能调优，否则不需要用到该参数。
     * 返回 {MatchingResult}
     * 在大图片中搜索小图片，并返回搜索结果MatchingResult。该函数可以用于找图时找出多个位置，可以通过max参数控制最大的结果数量。也可以对匹配结果进行排序、求最值等操作。
     */
    fun matchTemplate(
        image: Image,
        template: Image,
        options: FindImageOptions? = definedExternally
    ): dynamic

    /**
     * images.findCircles(gray, options)
     * gray {Image} 灰度图片
     * options {Object} 选项包括：
     * region {Array} 找圆区域。是一个两个或四个元素的数组。(region[0], region[1])表示找圆区域的左上角；region[2]*region[3]表示找圆区域的宽高。如果只有region只有两个元素，则找圆区域为(region[0], region[1])到图片右下角。如果不指定region选项，则找圆区域为整张图片。
     * dp {number} dp是累加面与原始图像相比的分辨率的反比参数，dp=2时累计面分辨率是元素图像的一半，宽高都缩减为原来的一半，dp=1时，两者相同。默认为1。
     * minDst {number} minDist定义了两个圆心之间的最小距离。默认为图片高度的八分之一。
     * param1 {number} param1是Canny边缘检测的高阈值，低阈值被自动置为高阈值的一半。默认为100，范围为0-255。
     * param2 {number} param2是累加平面对是否是圆的判定阈值，默认为100。
     * minRadius {number} 定义了检测到的圆的半径的最小值，默认为0。
     * maxRadius {number} 定义了检测到的圆的半径的最大值，0为不限制最大值，默认为0。
     * return {Array}
     * 在图片中寻找圆（做霍夫圆变换）。找到时返回找到的所有圆{x,y,radius}的数组，找不到时返回null。
     */
    fun findCircles(
        image: Image,
        options: FindCirclesOptions
    ): Array<dynamic /* Point | Point */>
}

@JsName("FindColorOptions")
class FindColorOptions {
    /**
     * {Array} 找色区域。是一个两个或四个元素的数组。
     * (region[0], region[1])表示找色区域的左上角；
     * region[2]*region[3]表示找色区域的宽高。
     * 如果只有region只有两个元素，则找色区域为(region[0], region[1])到屏幕右下角。
     * 如果不指定region选项，则找色区域为整张图片。
     */
    @JsName("region")
    var region: Array<Int>? = null /* [Int, Int] | [Int, Int, Int, Int] */

    /**
     * 找色时颜色相似度的临界值 0-255
     */
    @JsName("color")
    var threshold: Int? = null
}

@JsName("FindImageOptions")
class FindImageOptions {
    /**
     * region {Array} 找图区域。参见findColor函数关于region的说明。
     */
    @JsName("region")
    var region: Array<Int>? = null /* [Int, Int] | [Int, Int, Int, Int] */

    /**
     * threshold {number} 找图时图片相似度，范围为0~1的浮点数。默认值为0.9。
     */
    @JsName("threshold")
    var threshold: Int? = null

    /**
     * level {number} 一般而言不必修改此参数。不加此参数时该参数会根据图片大小自动调整。找图算法是采用图像金字
     */
    @JsName("level")
    var level: Int? = null

    /**
     * max {number} 找图时最多返回的匹配结果数量，默认值为5。
     */
    @JsName("max")
    val max: Int? = null
}

@JsName("FindCirclesOptions")
class FindCirclesOptions {
    @JsName("region")
    var region: Array<Int>? = null

    @JsName("dp")
    var dp: Int? = null

    @JsName("minDst")
    var minDst: Int? = null

    @JsName("param1")
    var param1: Int? = null

    @JsName("param2")
    var param2: Int? = null

    @JsName("minRadius")
    var minRadius: Int? = null

    @JsName("maxRadius")
    var maxRadius: Int? = null
}


@JsName("MatchingResult")
external interface MatchingResult {
    @JsName("matches")
    var matches: Array<Match>

    @JsName("first")
    fun first(): Match?

    @JsName("last")
    fun last(): Match?

    @JsName("leftmost")
    fun leftmost(): Match?

    @JsName("topmost")
    fun topmost(): Match?

    @JsName("rightmost")
    fun rightmost(): Match?

    @JsName("bottommost")
    fun bottommost(): Match?

    @JsName("best")
    fun best(): Match?

    @JsName("worst")
    fun worst(): Match?

    @JsName("sortBy")
    fun sortBy(cmp: dynamic): MatchingResult
}

@JsName("Match")
external interface Match {
    @JsName("point")
    var point: Point

    @JsName("similarity")
    var similarity: Double
}


external interface Image {
    fun getWidth(): Int
    fun getHeight(): Int
    fun saveTo(path: String)
    fun pixel(x: Int, y: Int): Int

    /**
     * Image对象通过调用recycle()函数来回收
     */
    fun recycle()
}

/**
 * 向系统申请屏幕截图权限，返回是否请求成功,仅需执行一次
 */
external fun requestScreenCapture(b: Boolean): Boolean
external fun requestScreenCapture(): Boolean

/**
 * 截图
 */
external fun captureScreen(path: String)
external fun captureScreen(): Image

external fun findColor(
    image: Image,
    color: dynamic /* Int | String */,
    options: FindColorOptions? = definedExternally
): Point

/**
 * 区域找色的简便方法。
 */
external fun findColorInRegion(
    image: Image,
    color: dynamic /* Int | String */,
    x: Int,
    y: Int,
    width: Int? = definedExternally,
    height: Int? = definedExternally,
    threshold: Int? = definedExternally
): Point

/**
 * 在图片img指定区域中找到颜色和color完全相等的某个点，并返回该点的左边；如果没有找到，则返回null。
 *
 * 找色区域通过x, y, width, height指定，如果不指定找色区域，则在整张图片中寻找。
 */
external fun findColorEquals(
    image: Image,
    color: dynamic /* Int | String */,
    x: Int? = definedExternally,
    y: Int? = definedExternally,
    width: Int? = definedExternally,
    height: Int? = definedExternally
): Point