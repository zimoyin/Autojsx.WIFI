var JavaScriptObfuscator = require('confusion.js');

//https://github.com/javascript-obfuscator/javascript-obfuscator
let options = {
    // 类型：boolean默认：true  紧凑的代码输出在一行上。
    compact: true,

    //启用代码控制流扁平化。控制流扁平化是一种阻碍程序理解的源代码结构转换。
    controlFlowFlattening: false,
    //扁平化使用概率
    //此设置对于大型代码特别有用，因为大量控制流转换会减慢代码速度并增加代码大小。
    controlFlowFlatteningThreshold: 0.75,

    // 插入死代码
    // 显着增加混淆代码的大小（最多 200%），仅当混淆代码的大小不重要时才使用。用于deadCodeInjectionThreshold设置受死代码注入影响的节点百分比。
    deadCodeInjection: false,
    //死代码影响率
    deadCodeInjectionThreshold: 0.4,

    //阻止调试
    debugProtection: false,
    //如果设置，则会使用以毫秒为单位的时间间隔来强制“控制台”选项卡上的调试模式，从而使使用开发人员工具的其他功能变得更加困难。
    // debugProtection如果启用则有效。建议值介于2000和4000毫秒之间。
    debugProtectionInterval: 0,
    //此选项禁用console所有脚本的全局调用
    //console.log禁用、console.info、 console.error、console.warn、的使用console.debug，console.exception并console.trace用空函数替换它们。这使得调试器的使用更加困难。
    disableConsoleOutput: false,

    //锁定代码，使其只能在本域名执行（复制到其他地方难以使用）
    //string[]
    domainLock: [],
    //如果源代码未在指定的域上运行，则允许将浏览器重定向到传递的 URLdomainLock
    domainLockRedirectUrl: 'about:blank',

    //启用字符串文字的强制转换，该字符串文字与传递的 RegExp 模式相匹配。
    //此选项仅影响不应被转换的字符串stringArrayThreshold（或将来可能的其他阈值）
    //该选项的优先级高于reservedStringsoption，但不高于conditional comments。
    //string[]
    forceTransformStrings: [],

    //此选项的主要目标是能够在混淆多个源/文件期间使用相同的标识符名称。
    identifierNamesCache: null,
    //设置标识符名称生成器。
    //标识符混淆方式，hexadecimal（十六进制）、mangled（短标识符）
    identifierNamesGenerator: 'hexadecimal',
    identifiersDictionary: [],
    //标识符添加特定前缀
    identifiersPrefix: '',

    //防止导入混淆require。在某些情况下，当由于某种原因运行时环境需要仅使用静态字符串进行导入时，这可能会有所帮助。
    ignoreImports: false,
    inputFileName: '',

    //启用将信息记录到控制台。
    log: false,

    //将字符串转化为表达式
    numbersToExpressions: false,

    optionsPreset: 'default',

    //这个选项可能会破坏你的代码。
    //允许使用声明来混淆全局变量和函数名称。
    renameGlobals: false,
    //启用属性名称重命名。所有内置 DOM 属性和核心 JavaScript 类中的属性都将被忽略。
    renameProperties: false,
    renamePropertiesMode: 'safe',
    reservedNames: [],
    reservedStrings: [],

    seed: 0,

    //谨慎使用
    //使用此选项进行混淆后，不要以任何方式更改混淆的代码，因为任何诸如格式化代码之类的更改都会触发自我防御，代码将不再工作！
    //该选项强制将compact值设置为true
    selfDefending: false,
    simplify: true,
    sourceMap: false,
    sourceMapBaseUrl: '',
    sourceMapFileName: '',
    sourceMapMode: 'separate',
    sourceMapSourcesMode: 'sources-content',
    splitStrings: false,
    splitStringsChunkLength: 10,
    stringArray: true,
    stringArrayCallsTransform: true,
    stringArrayCallsTransformThreshold: 0.5,
    stringArrayEncoding: [],
    stringArrayIndexesType: [
        'hexadecimal-number'
    ],
    stringArrayIndexShift: true,
    stringArrayRotate: true,
    stringArrayShuffle: true,
    stringArrayWrappersCount: 1,
    stringArrayWrappersChainedCalls: true,
    stringArrayWrappersParametersMaxCount: 2,
    stringArrayWrappersType: 'variable',
    stringArrayThreshold: 0.75,
    target: 'browser',
    transformObjectKeys: false,
    unicodeEscapeSequence: false
}

var obfuscationResult = JavaScriptObfuscator.obfuscate(
    `
        (function(){
            var variable1 = '5' - 3;
            var variable2 = '5' + 3;
            var variable3 = '5' + - '2';
            var variable4 = ['10','10','10','10','10'].map(parseInt);
            var variable5 = 'foo ' + 1 + 1;
            console.log(variable1);
            console.log(variable2);
            console.log(variable3);
            console.log(variable4);
            console.log(variable5);
        })();
    `,options
);
// obfuscationResult.getObfuscatedCode()// 返回string混淆代码；
// obfuscationResult.getSourceMap()// 如果sourceMap启用选项 -如果选项设置为 ，则返回string源映射或空字符串；sourceMapModeinline
console.log(obfuscationResult.getObfuscatedCode());