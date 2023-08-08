/**
 * 参数列表,只使用第一个参数，该参数是一个URL。 请使用程序在文件最前面进行添加
 */
// let values = [];


let array = [];
//运行中的脚本
for (let item of engines.all()) {
    let a = {
        engineId: item.id,
        sourceName: item.source.toString(),
        source: item.source.script,
        engineScriptCwd: item.cwd(),
        engineScriptArgv: item.execArgv,
        isStopped: item.isDestroyed()
    }
    let jsonText = JSON.stringify(a);
    let parsedObject = JSON.parse(jsonText);
    array.push(parsedObject)
}

// var url = "www.baidu.com";
// let url = "http://192.168.0.103:8080/receive";
let data = {
    cmd: 0,
    message: "getRunningList",
    ID: values[1],
    value: array,
};

let res = http.postJson(values[0], data);
if (res.statusCode === 200) {
    // toast("请求成功");
    // log(res.body.string());
    console.log("CMD 访问成功: "+data.message+" ID: "+data.ID+"  send data: "+data.message);
} else {
    // toast("请求失败:" + res.statusMessage);
    console.log("CMD 访问失败: "+data.message+" ID: "+data.ID)
}
