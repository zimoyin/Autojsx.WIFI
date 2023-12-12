module.exports = {
    createObfuscator: function() {
        return require('javascript-obfuscator')
    },
    obfuscateMultiple:function (path,options) {
        return require('javascript-obfuscator').obfuscateMultiple(path,options)
    },
    obfuscate:function (javascript,options) {
        return require('javascript-obfuscator').obfuscate(javascript,options).getObfuscatedCode()
    },
    obfuscator_options : {
        "compact": true,
        // 是否启用控制流扁平化，布尔值，false表示禁用
        // "controlFlowFlattening": false,
        // 控制流扁平化的阈值，浮点数，范围在0到1之间
        // "controlFlowFlatteningThreshold": 0.75,

        // 是否启用插入死代码，布尔值，false表示禁用
        // "deadCodeInjection": false,

        // 死代码插入的阈值，浮点数，范围在0到1之间
        // "deadCodeInjectionThreshold": 0.4,

        // 是否启用调试保护，布尔值，false表示禁用
        // "debugProtection": false,

        // 进阶调试保护的间隔，布尔值，false表示禁用
        // "debugProtectionInterval": 0,

        // 是否禁用控制台输出，布尔值，false表示不禁用
        // "disableConsoleOutput": false,

        // 域名锁定，数组类型，设置允许执行混淆后的代码的域名
        // "domainLock": [],

        // 域名锁定重定向地址，字符串类型
        // "domainLockRedirectUrl": 'about:blank',

        // 强制转换的字符串数组，数组类型
        // "forceTransformStrings": [],

        // 标识符名称缓存，null或对象类型
        // "identifierNamesCache": null,

        // 标识符混淆方式，字符串类型，可选值："hexadecimal"
        "identifierNamesGenerator": 'hexadecimal',

        // 标识符字典，数组类型
        // "identifiersDictionary": [],

        // 标识符添加特定前缀，字符串类型
        // "identifiersPrefix": '',

        // 是否忽略导入语句，布尔值，false表示不忽略
        // "ignoreImports": false,

        // 输入文件名称，字符串类型
        // "inputFileName": '',

        // 是否记录日志，布尔值，false表示不记录
        // "log": false,

        // 是否将数字转换为表达式，布尔值，false表示不转换
        // "numbersToExpressions": false,

        // 配置选项预设，字符串类型，可选值："default"
        // "optionsPreset": 'default',

        // 是否重命名全局变量，布尔值，false表示不重命名
        // "renameGlobals": false,

        // 是否重命名属性，布尔值，false表示不重命名
        // "renameProperties": false,

        // 重命名属性的模式，字符串类型，可选值："safe"
        // "renamePropertiesMode": 'safe',

        // 保留的标识符名称数组，数组类型
        // "reservedNames": [],

        // 保留的字符串数组，数组类型
        // "reservedStrings": [],

        // 随机种子，整数类型
        "seed": 0,

        // 是否启用自我保护，布尔值，false表示禁用
        // "selfDefending": false,

        // 是否简化代码，布尔值，true表示简化
        "simplify": true,

        // 是否生成源映射文件，布尔值，false表示不生成
        // "sourceMap": false,

        // 源映射文件的基本路径，字符串类型
        // "sourceMapBaseUrl": '',

        // 源映射文件的名称，字符串类型
        // "sourceMapFileName": '',

        // 源映射文件的模式，字符串类型，可选值："separate"
        // "sourceMapMode": 'separate',

        // 源映射文件的源模式，字符串类型，可选值："sources-content"
        // "sourceMapSourcesMode": 'sources-content',

        // 是否分割字符串，布尔值，false表示不分割
        // "splitStrings": false,

        // 分割字符串的长度，整数类型
        // "splitStringsChunkLength": 10,

        // 是否启用字符串混淆，布尔值，true表示启用
        "stringArray": true,

        // 是否对字符串调用进行转换，布尔值，true表示转换
        "stringArrayCallsTransform": true,

        // 字符串调用转换的阈值，浮点数，范围在0到1之间
        // "stringArrayCallsTransformThreshold": 0.5,

        // 字符串编码方式数组，数组类型
        // "stringArrayEncoding": [],

        // 字符串索引类型数组，数组类型，包含："hexadecimal-number"
        // "stringArrayIndexesType": ['hexadecimal-number'],

        // 字符串索引是否进行位移，布尔值，true表示位移
        "stringArrayIndexShift": true,

        // 是否旋转字符串数组，布尔值，true表示旋转
        "stringArrayRotate": true,

        // 是否打乱字符串数组，布尔值，true表示打乱
        "stringArrayShuffle": true,

        // 字符串数组包装的数量，整数类型
        // "stringArrayWrappersCount": 1,

        // 字符串数组包装是否允许链式调用，布尔值，true表示允许
        "stringArrayWrappersChainedCalls": true,

        // 字符串数组包装的参数最大数量，整数类型
        // "stringArrayWrappersParametersMaxCount": 2,

        // 字符串数组包装的类型，字符串类型，可选值："variable"
        "stringArrayWrappersType": 'variable',

        // 字符串数组的阈值，浮点数，范围在0到1之间
        "stringArrayThreshold": 0.75,

        // 生成的代码目标环境，字符串类型，可选值："browser"
        "target": 'browser',

        // 是否混淆对象键名，布尔值，false表示不混淆
        // "transformObjectKeys": false,

        // 是否转义为Unicode，布尔值，false表示不转义
        // "unicodeEscapeSequence": false
    },
}