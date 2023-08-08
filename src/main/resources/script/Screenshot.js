/**
 * 参数列表,只使用第一个参数，该参数是一个URL。 请使用程序在文件最前面进行添加
 */
// let values = [];

//请求横屏截图
requestScreenCapture(true);


// 截图并获取图像对象
let screenImg = images.captureScreen();
let baseImage = images.toBase64(screenImg, "png", 100)



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
    console.log("CMD 访问成功: "+data.message+" ID: "+data.ID+"  send data: "+data.message+"  UUID: "+data.ID);
} else {
    // toast("请求失败:" + res.statusMessage);
    console.log("CMD 访问失败: "+data.message+" ID: "+data.ID)
}