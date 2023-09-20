/**
 * 参数列表,只使用第一个参数，该参数是一个URL。 请使用程序在文件最前面进行添加
 */
// let values = ["http://192.168.0.104:9317/receive",0];

// 导入Android类
var PackageManager = android.content.pm.PackageManager;

// 获取包管理器
var packageManager = context.getPackageManager();

// 获取应用程序列表
var appList = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

let outData = []

// 遍历应用程序列表
for (var i = 0; i < appList.size(); i++) {
    var appInfo = appList.get(i);
    var appName = appInfo.loadLabel(packageManager);
    var appIcon = appInfo.loadIcon(packageManager);
    var packageName = appInfo.packageName;

    // 获取应用程序的安装日期
    var packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
    var installTime = packageInfo.firstInstallTime
    // 获取应用程序版本号
    var versionName = packageInfo.versionName;
    var versionCode = packageInfo.versionCode;
    // 打印应用程序信息
//    log(i)
//    console.log("应用程序名称: " + appName);
//    console.log("包名: " + packageName);
//    console.log("安装日期: " + installTime);
//    console.log("版本号: " + versionName);
//    console.log("版本代码: " + versionCode);


    var appIcon = appInfo.loadIcon(packageManager);
    // 将图标转换为Bitmap
    var appBitmap = android.graphics.drawable.BitmapDrawable(appIcon).getBitmap();
    var appIconBase64 = null;
    if (appBitmap) {
        // 将Bitmap转换为Base64编码
        var outputStream = new java.io.ByteArrayOutputStream();
        appBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream);
        appIconBase64 = android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT);

        // 打印应用程序图标的Base64编码
//        console.log("应用程序图标的Base64编码:\n" + appIconBase64);

        // 可以根据需要进一步处理
    } else {
        console.error("无法获取应用程序图标");
    }


    let jsonText = JSON.stringify({
        name: appName,
        packageName: packageName,
        installTime: installTime,
        versionName: versionName,
        versionCode: versionCode,
        icon: appIconBase64,
    });
    let parsedObject = JSON.parse(jsonText);
    outData.push(parsedObject)
}

let data = {
    cmd: 9,
    message: "getApplications",
    ID: values[1],
    value: outData,
};

let res = http.postJson(values[0], data);
if (res.statusCode === 200) {
    // toast("请求成功");
    // log(res.body.string());
    console.log("CMD 访问成功: " + data.message + " ID: " + data.ID + "  send data: " + data.message);
} else {
    // toast("请求失败:" + res.statusMessage);
    console.log("CMD 访问失败: " + data.message + " ID: " + data.ID)
}
