/**
 * 参数列表,只使用第一个参数，该参数是一个name。 请使用程序在文件最前面进行添加
 */
// let values = [];

//运行中的脚本
for (let item of engines.all()) {
    if(item.source.toString() === values[0]){
        item.forceStop()
        console.log("Server 停止脚本 by name: "+values[0]);
        exit()
    }
}
console.log("脚本未运行 by name: "+values[0]);
