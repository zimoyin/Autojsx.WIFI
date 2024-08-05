// const { findNode } = require("common/common");
/**
 * 参数列表,只使用第一个参数，该参数是一个URL。 请使用程序在文件最前面进行添加
 */
// let values = [];


// 获取屏幕上的所有节点，有父子从属描述
var allNodes = find();

// 构建XML数据
var xmlData = '<?xml version="1.0" encoding="UTF-8"?>\n<hierarchy rotation="0">\n';


// 定义函数，递归构建节点信息
function buildNodeInfo(node, index) {
    //控件属性如下
    //depth 深度也是控件属性，他描述了控件在xml中的深度。控件的深度是指控件在布局层次结构中的嵌套深度，根节点的深度为0，其子节点深度为1，以此类推。
    var bounds = node.bounds();
    var left = bounds.left;
    var top = bounds.top;
    var right = bounds.right;
    var bottom = bounds.bottom;
    var width = right - left;
    var height = bottom - top;

    // bounds: 控件的坐标范围，包含左上角和右下角的坐标。
    var boundsString = "[" + bounds.left + ", " + bounds.top + ", " + bounds.right + ", " + bounds.bottom + "]";
    var nodeId = node.id();
    var className = node.className();
    var description = node.desc();
    var text = node.text();
    var packageName = node.packageName(); // String
    var checkable = node.checkable();
    var checked = node.checked();
    var clickable = node.clickable();
    var enabled = node.enabled();
    var focusable = node.focusable();
    var focused = node.focused();
    var scrollable = node.scrollable();
    var longClickable = node.longClickable();
    var password = node.password();
    var selected = node.selected();
    var depth = node.depth();

    // 可能是桌面
    var applicationPackageName = "";
    if (packageName.indexOf("launcher") !== -1){
        if (text !== null && text !== undefined && text !== ""){
            applicationPackageName = getPackageName(text)
        }
    }

    var indent = '  '.repeat(depth);
    if (nodeId === null || nodeId === undefined) nodeId = "";
    if (description === null || description === undefined) description = "";
    xmlData += `<node index="${index}" depth="${depth}" text="${text}" resource-id="${nodeId}" class="${className}" packageName="${packageName}" applicationPackageName="${applicationPackageName}" content-desc="${description}" bounds="${boundsString}" checkable="${checkable}" checked="${checked}" clickable="${clickable}" enabled="${enabled}" focusable="${focusable}" focused="${focused}" scrollable="${scrollable}" long-clickable="${longClickable}" password="${password}" selected="${selected}" bundle="${boundsString}" left="${left}" top="${top}" right="${right}" bottom="${bottom}" width="${width}" height="${height}"`

    if (node.children().length === 0) {
        xmlData += "/>"
    } else {
        xmlData += ">"
    }

    // 获取子节点信息
    var childNodes = node.children();
    var childIndex = 0;
    for (var i = 0; i < childNodes.length; i++) {
        buildNodeInfo(childNodes[i], childIndex);
        childIndex++
    }

    if (node.children().length !== 0) {
        xmlData += indent + '</node>\n';
    }

}

// 遍历所有节点，将节点信息添加到XML中
var root_index = 0;
for (var i = 0; i < allNodes.length; i++) {
    var node = allNodes[i];
    if (!node.parent()) { // 根节点没有父节点
        buildNodeInfo(node, root_index);
        root_index++
    }
}

xmlData += '</hierarchy>';

// 保存XML数据到文件
// var filePath = "/sdcard/nodes.xml"; // 您可以自行指定保存的路径和文件名
// files.write(filePath, xmlData);

// console.log("已保存节点信息到文件：" + filePath);

// log(xmlData)

let data = {
    cmd: 1,
    message: "getNodes",
    ID: values[1],
    value: xmlData,
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
