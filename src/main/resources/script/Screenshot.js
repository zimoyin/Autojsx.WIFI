/**
 * 参数列表,只使用第一个参数，该参数是一个URL。 请使用程序在文件最前面进行添加
 */
// let values = [];

var width = device.width;
var height = device.height;

var isLandscape = width > height;

//请求横屏截图
requestScreenCapture(isLandscape);
// 截图并获取图像对象
let screenImg = images.captureScreen();
let baseImage = images.toBase64(screenImg, "png", 100)

if (!checkScreenshot(screenImg, isLandscape)) {
    //请求横屏截图
    requestScreenCapture(!isLandscape);
    // 截图并获取图像对象
    let screenImg = images.captureScreen();
    baseImage = images.toBase64(screenImg, "png", 100)
}

// 判断截图的空白区域
function checkScreenshot(img, isLandscape) {
    var isTransparent = false;

    if (isLandscape) {
        // 横屏模式：检查顶部和底部区域
        for (var y = 0; y < height / 4; y++) {
            if ((images.pixel(img, width / 2, y) >>> 24) === 0) {
                isTransparent = true;
                break;
            }
        }
        if (!isTransparent) {
            for (var y = height * 3 / 4; y < height; y++) {
                if ((images.pixel(img, width / 2, y) >>> 24) === 0) {
                    isTransparent = true;
                    break;
                }
            }
        }
    } else {
        // 竖屏模式：检查左侧和右侧区域
        for (var x = 0; x < width / 4; x++) {
            if ((images.pixel(img, x, height / 2) >>> 24) === 0) {
                isTransparent = true;
                break;
            }
        }
        if (!isTransparent) {
            for (var x = width * 3 / 4; x < width; x++) {
                if ((images.pixel(img, x, height / 2) >>> 24) === 0) {
                    isTransparent = true;
                    break;
                }
            }
        }
    }
    return !isTransparent
}


let data = {
    cmd: 3,
    message: "getScreenshot",
    ID: values[1],
    value: baseImage,
};

let res = http.postJson(values[0], data);
if (res.statusCode === 200) {
    // toast("请求成功");
    // log(res.body.string());
    console.log("CMD 访问成功: " + data.message + " ID: " + data.ID + "  send data: " + data.message + "  UUID: " + data.ID);
} else {
    // toast("请求失败:" + res.statusMessage);
    console.log("CMD 访问失败: " + data.message + " ID: " + data.ID)
}