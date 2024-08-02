// 是否启用代码压缩
const isMinimize = false

// 是否启用JS 版本降级, 当前启用代码压缩后，可以不使用 JS 降级, 因为大部分情况下压缩后的代码没有使用 Rhino 引擎支持之外的语法
const isDowngrade = true

// 是否混淆代码
const isJavascriptObfuscator = false
const isJavascriptObfuscator_Compact = true //混淆时是否压缩代码,该压缩不会更改语法特性，因此还需要第一个压缩或者降级

if (isMinimize === false && isDowngrade === false){
    throw new Error('At least one of isMinimize and isDowngrade must be true')
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
const path = require('path');
const JavascriptObfuscator = require("webpack-obfuscator");
const projectName = path.basename(__dirname);// 获取当前文件夹的名称
module.exports = {
    entry: `./kotlin/${projectName}.mjs`, // 修改为生成的 JS 文件的路径
    output: {
        path: path.resolve(__dirname, 'build/output'),
        filename: 'main.js'
    },
    mode: 'production', //  'production' , 'development'
    module: {
        rules: [
            babel()
        ]
    },
    optimization: {
        minimize: isMinimize
    },
    plugins: [
        javascriptObfuscator()
    ].filter(Boolean) // Filter out null values
};

function babel() {
    if (!isDowngrade) {
        return {}
    } else {
        return {
            test: /\.(m?js)$/,
            exclude: /node_modules/,
            use: {
                loader: 'babel-loader',
                options: {
                    // 配置 babel 将 ES6 转为 ES5
                    presets: [
                        ["@babel/preset-env", {
                            "targets": {
                                "ie": "11"
                            },
                            "modules": "auto", // false , commonjs , amd , systemjs , umd , auto
                            "useBuiltIns": "entry", // false , usage , entry
                            "corejs": 3
                        }]
                    ]
                }
            }
        }
    }
}

function javascriptObfuscator() {
    if (!isJavascriptObfuscator) return null
    return new JavascriptObfuscator({
        /* 压缩 */
        compact: isJavascriptObfuscator_Compact,
        /* 控制流扁平化（降低50%速度） */
        // controlFlowFlattening: true,
        /* 扁平化使用概率 */
        // controlFlowFlatteningThreshold: 0.75,
        /* 插入死代码 */
        // deadCodeInjection: true,
        /* 死代码影响率 */
        // deadCodeInjectionThreshold: 0.4,
        /* 阻止调试 */
        // debugProtection: false,
        /* 进阶阻止调试 */
        // debugProtectionInterval: false,
        /* 禁用console */
        // disableConsoleOutput: false,
        /* 锁定代码，使其只能在本域名执行（复制到其他地方难以使用） */
        // domainLock: [],
        /* 标识符混淆方式，hexadecimal（十六进制）、mangled（短标识符） */
        identifierNamesGenerator: 'hexadecimal',
        /* 标识符添加特定前缀 */
        // identifiersPrefix: '',
        /* 允许将信息记录到控制台 */
        // inputFileName: '',
        // log: false,
        /* 启用全局变量和函数名你的混淆 */
        renameGlobals: true,
        /* 混淆器会对对象的属性名进行重命名。这包括对象字面量、类的属性以及任何通过点符号或方括号语法设置的属性。 */
        renameProperties: true,
        /* 禁用模糊处理和生成标识符 */
        reservedNames: ['main'],
        /* 禁用数组内字符串的转换 */
        // reservedStrings: [],
        /* 通过固定和随机的位置移动数组，使解密的位置难以匹配，大文件应重点开启 */
        rotateStringArray: true,
        seed: 0,
        /* 使混淆后的代码无法使用格式美化，需要保证compact为true */
        // selfDefending: isJavascriptObfuscator_Compact && true,
        /* 生成指引文件 */
        // sourceMap: false,
        // sourceMapBaseUrl: '',
        // sourceMapFileName: '',
        // sourceMapMode: 'separate',
        /* 删除字符串，并将它们放在一个数组中使用 */
        stringArray: true,
        /* 编码字符串  none, base64, rc4 */
        // stringArrayEncoding: ["rc4"],
        /* 编码率 */
        // stringArrayThreshold: 0.75,
        /* 生成的代码环境，可选Browser、Browser No Eval、Node */
        target: 'node',
        /* 混淆对象键名 */
        transformObjectKeys: true,
        /* 转义为Unicode，会大大增加体积，还原也比较容易，建议只对小文件使用 */
        // unicodeEscapeSequence: true,
    })
}
