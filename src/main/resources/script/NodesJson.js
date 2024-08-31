// const { findNode } = require("common/common");
/**
 * 参数列表,只使用第一个参数，该参数是一个URL。 请使用程序在文件最前面进行添加
 */
// let values = [];

let startTime = Date.now();
// 获取屏幕上的所有节点，有父子从属描述
var allNodes = find();
var count = 0

// 定义函数，递归构建节点信息
function buildNodeInfo(node, index) {
    count++
    var bounds = node.bounds();
    var left = bounds.left;
    var top = bounds.top;
    var right = bounds.right;
    var bottom = bounds.bottom;
    var width = right - left;
    var height = bottom - top;

    var nodeId = node.id() || "";
    var className = node.className() || "";
    var description = node.desc() || "";
    var text = node.text() || "";
    var packageName = node.packageName() || ""; // String
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
    var editable = false;
    var multiLine = false;
    var drawingOrder = -1;
    try {
        try{
            editable = node.isEditable();
        }catch (e){}
        try{
            multiLine = node.isMultiLine();
        }catch (e){}
        drawingOrder = node.getDrawingOrder();
    } catch (e) {
    }

    var boundsString = "[" + bounds.left + "," + bounds.top + "][" + bounds.right + "," + bounds.bottom + "]";

    var applicationPackageName = "";
    if (packageName.indexOf("launcher") !== -1) {
        if (text) {
            applicationPackageName = getPackageName(text);
            if (applicationPackageName === null || applicationPackageName === undefined) applicationPackageName = ""
        }
    }

    if (className === null || className === undefined) className = ""

    var nodeInfo = {
        index: index,
        depth: depth,
        text: text,
        resourceId: nodeId,
        className: className,
        packageName: packageName,
        applicationPackageName: applicationPackageName,
        contentDesc: description,
        bounds: {
            str: boundsString,
            left: left,
            top: top,
            right: right,
            bottom: bottom,
            width: width,
            height: height
        },
        checkable: checkable,
        checked: checked,
        clickable: clickable,
        enabled: enabled,
        focusable: focusable,
        focused: focused,
        scrollable: scrollable,
        longClickable: longClickable,
        password: password,
        selected: selected,
        editable: editable,
        drawingOrder: drawingOrder,
        multiLine: multiLine,
        childNodes: []
    };

    var childNodes = node.children();
    var childIndex = 0;
    for (var i = 0; i < childNodes.length; i++) {
        nodeInfo.childNodes.push(buildNodeInfo(childNodes[i], childIndex))
        childIndex++
    }

    return nodeInfo;
}

// 遍历所有节点，将节点信息添加到JSON中
var root_index = 0;
var rootNodes = [];
for (var i = 0; i < allNodes.length; i++) {
    var node = allNodes[i];
    if (!node.parent()) { // 根节点没有父节点
        rootNodes.push(buildNodeInfo(node, root_index));
        root_index++
    }
}

// 构建JSON数据
var jsonData = {
    hierarchy: rootNodes
};
let endTime = Date.now();
console.log(`构建节点信息耗时: ${endTime - startTime} milliseconds.`);

startTime = Date.now();
// 发送JSON数据
let data = {
    cmd: 9,
    message: "getNodesJson",
    ID: values[1],
    value: JSON.stringify(jsonData)
};
endTime = Date.now();
console.log(`节点信息转义为JSON数据耗时: ${endTime - startTime} milliseconds.`);

startTime = Date.now();
let res = http.postJson(values[0], data);
if (res.statusCode === 200) {
    console.log("CMD 访问成功: " + data.message + " ID: " + data.ID + "  send data: " + data.message);
} else {
    console.log("CMD 访问失败: " + data.message + " ID: " + data.ID);
}
endTime = Date.now();
console.log(`发送数据耗时: ${endTime - startTime} milliseconds.`);
