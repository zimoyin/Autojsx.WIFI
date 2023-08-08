package github.zimo.autojsx.server

import io.vertx.core.json.JsonObject

fun main() {
  VertxServer.start(8080)

//  val s = "{\"cmd\":0,\"message\":\"getRunningList\",\"ID\":\"2630cac5-2f88-4e1d-a2fc-e07a4994f0a4\",\"value\":[{\"engineId\":21,\"srourceName\":\"[remote]getRunningList.js\",\"srource\":\"let values = ['http://192.168.0.103:8080/receive','2630cac5-2f88-4e1d-a2fc-e07a4994f0a4'];\\r\\n/**\\r\\n * 参数列表,只使用第一个参数，该参数是一个URL。 请使用程序在文件最前面进行添加\\r\\n */\\r\\n// let values = [];\\r\\n\\r\\n\\r\\nlet array = [];\\r\\n//运行中的脚本\\r\\nfor (let item of engines.all()) {\\r\\n    let a = {\\r\\n        engineId: item.id,\\r\\n        srourceName: item.source.toString(),\\r\\n        srource: item.source.script,\\r\\n        engineScriptCwd: item.cwd(),\\r\\n        engineScriptArgv: item.execArgv,\\r\\n        isStoped: item.isDestroyed()\\r\\n    }\\r\\n    let jsonText = JSON.stringify(a);\\r\\n    let parsedObject = JSON.parse(jsonText);\\r\\n    array.push(parsedObject)\\r\\n}\\r\\n\\r\\n// var url = \\\"www.baidu.com\\\";\\r\\n// let url = \\\"http://192.168.0.103:8080/receive\\\";\\r\\nlet data = {\\r\\n    cmd: 0,\\r\\n    message: \\\"getRunningList\\\",\\r\\n    ID: values[1],\\r\\n    value: array,\\r\\n};\\r\\n\\r\\nlet res = http.postJson(values[0], data);\\r\\nif (res.statusCode === 200) {\\r\\n    // toast(\\\"请求成功\\\");\\r\\n    // log(res.body.string());\\r\\n    console.log(\\\"CMD 访问成功: \\\"+data.message+\\\" ID: \\\"+data.ID+\\\"  send data: \\\"+data.toString());\\r\\n} else {\\r\\n    // toast(\\\"请求失败:\\\" + res.statusMessage);\\r\\n    console.log(\\\"CMD 访问失败: \\\"+data.message+\\\" ID: \\\"+data.ID)\\r\\n}\\r\\n\",\"engineScriptCwd\":\"/storage/emulated/0/脚本\",\"engineScriptArgv\":{},\"isStoped\":false},{\"engineId\":5,\"srourceName\":\"/storage/emulated/0/脚本/test.js\",\"srource\":\"while(true){\\r\\n\\r\\n}\",\"engineScriptCwd\":\"/storage/emulated/0/脚本\",\"engineScriptArgv\":{},\"isStoped\":false}]}\n"
//
//
//  println(JsonObject(s).getJsonArray("value"))

}
