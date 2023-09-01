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
    /*debugProtection: false,*/
    //如果设置，则会使用以毫秒为单位的时间间隔来强制“控制台”选项卡上的调试模式，从而使使用开发人员工具的其他功能变得更加困难。
    // debugProtection如果启用则有效。建议值介于2000和4000毫秒之间。
    /*debugProtectionInterval: 0,*/
    //此选项禁用console所有脚本的全局调用
    //console.log禁用、console.info、 console.error、console.warn、的使用console.debug，console.exception并console.trace用空函数替换它们。这使得调试器的使用更加困难。
    disableConsoleOutput: false,

    //锁定代码，使其只能在本域名执行（复制到其他地方难以使用）
    //string[]
    //此选项不适用于target: 'node'
    /*domainLock: [],*/
    //如果源代码未在指定的域上运行，则允许将浏览器重定向到传递的 URLdomainLock
    //此选项不适用于target: 'node'
    /*domainLockRedirectUrl: 'about:blank',*/

    //启用字符串文字的强制转换，该字符串文字与传递的 RegExp 模式相匹配。
    //此选项仅影响不应被转换的字符串stringArrayThreshold（或将来可能的其他阈值）
    //该选项的优先级高于reservedStringsoption，但不高于conditional comments。
    //string[]
    //?
    forceTransformStrings: [],

    /*//此选项的主要目标是能够在混淆多个源/文件期间使用相同的标识符名称。
    identifierNamesCache: null,
    //设置标识符名称生成器。
    //标识符混淆方式，hexadecimal（十六进制）、mangled（短标识符）
    identifierNamesGenerator: 'hexadecimal',
    identifiersDictionary: [],
    //标识符添加特定前缀
    identifiersPrefix: '',*/

    //防止导入混淆require。在某些情况下，当由于某种原因运行时环境需要仅使用静态字符串进行导入时，这可能会有所帮助。
    ignoreImports: true,
    inputFileName: '',

    //启用将信息记录到控制台。
    /*log: false,*/

    //将字符串转化为表达式
    numbersToExpressions: false,


    /**
     * 类型：string默认：default
     * 允许设置选项预设。
     * 可用值：
     * default;
     * low-obfuscation;
     * medium-obfuscation;
     * high-obfuscation。
     * 所有添加选项将与选定的预设选项合并。
     */
    optionsPreset: 'default',

    /**
     * 这个选项可能会破坏你的代码。
     * 允许使用声明来混淆全局变量和函数名称。
     */
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
    /*selfDefending: false,*/

    //通过简化启用额外的代码混淆。
    simplify: true,

    /**
     * 启用混淆代码的源映射生成。
     *
     * 源映射可帮助您调试混淆的 JavaScript 源代码。
     * 如果您想要或需要在生产中进行调试，您可以将单独的源映射文件上传到一个秘密位置，然后将浏览器指向那里。
     *
     * 由于是部署中使用，将不会使用此选
     */
    sourceMap: false,
    sourceMapBaseUrl: '',
    sourceMapFileName: '',
    sourceMapMode: 'separate',
    sourceMapSourcesMode: 'sources-content',

    /**
     * 将文字字符串拆分为具有splitStringsChunkLength选项值长度的块。
     */
    splitStrings: false,
    //设置splitStrings选项的块长度。
    splitStringsChunkLength: 1,

    /**
     * 删除字符串文字并将它们放入特殊数组中。
     */
    stringArray: true,
    //必须启用 stringArray
    //启用对stringArray. 这些调用的所有参数都可以根据值提取到不同的对象stringArrayCallsTransformThreshold。
    // 因此，自动查找对字符串数组的调用变得更加困难。
    stringArrayCallsTransform: true,
    //您可以使用此设置来调整对字符串数组的调用被转换的概率（从 0 到 1）。
    stringArrayCallsTransformThreshold: 0.5,
    /**
     * 此选项可能会减慢您的脚本速度。
     *
     * stringArray使用base64or对所有字符串文字进行编码rc4，并插入一个特殊代码，用于在运行时将其解码回来。
     *
     * 每个stringArray值都将通过从传递的列表中随机选择的编码进行编码。这使得使用多种编码成为可能。
     *
     * 可用值：
     *
     * 'none'( boolean): 不编码stringArray值
     * 'base64'( string)：stringArray使用编码值base64
     * 'rc4'( string)：stringArray使用 编码值rc4。
     * 比 慢约 30-50% base64，但更难获得初始值。
     * unicodeEscapeSequence建议在使用编码时禁用该选项rc4，以防止出现过大的混淆代码。
     */
    stringArrayEncoding: [],
    /**
     * 每个stringArray调用索引都将根据传递列表中随机选择的类型进行转换。这使得使用多种类型成为可能。
     *
     * 可用值：
     *
     * 'hexadecimal-number'( default)：将字符串数组调用索引转换为十六进制数字
     * 'hexadecimal-numeric-string'：将字符串数组调用索引转换为十六进制数字字符串
     * 2.9.0在发布之前javascript-obfuscator，将所有字符串数组调用索引转换为hexadecimal-numeric-string类型。这使得一些手动反混淆变得稍微困难​​，但它允许自动反混淆器轻松检测这些调用。
     *
     * 新hexadecimal-number类型使代码中字符串数组调用模式的自动检测更加困难。
     */
    stringArrayIndexesType: [
        'hexadecimal-number'
    ],
    //为所有字符串数组调用启用额外的索引移位
    stringArrayIndexShift: true,
    //将stringArray数组移动固定和随机（在代码混淆时生成）位置。这使得将删除的字符串的顺序与其原始位置匹配变得更加困难。
    stringArrayRotate: true,
    //随机打乱stringArray数组项。
    stringArrayShuffle: true,
    //设置每个根或函数范围内的包装器数量string array。每个范围内的包装器的实际数量受到literal该范围内节点数量的限制。
    stringArrayWrappersCount: 1,
    //启用包装器之间的链式调用string array。
    stringArrayWrappersChainedCalls: true,
    //允许控制字符串数组包装器参数的最大数量。默认值和最小值为2。推荐值介于2和之间5。
    stringArrayWrappersParametersMaxCount: 2,
    /**
     * 'variable'：在每个作用域的顶部附加变量包装器。性能快。
     * 'function'：在每个作用域内的随机位置附加函数包装器。性能比 with 慢variable，但提供更严格的混淆。
     */
    stringArrayWrappersType: 'variable',
    //此设置对于较大的代码特别有用，因为它会重复调用string array并会减慢代码速度。
    stringArrayThreshold: 0.75,


    /**
     * 允许为混淆代码设置目标环境：
     *  browser
     *  browser-no-eval
     *  node
     */
    target: 'node',

    /**
     * 启用对象键的转换。
     */
    transformObjectKeys: false,

    /**
     * 允许启用/禁用字符串转换为 unicode 转义序列。
     *
     * Unicode 转义序列大大增加了代码大小，并且字符串可以轻松恢复到其原始视图。建议仅针对小型源代码启用此选项。
     */
    unicodeEscapeSequence: false
}
// obfuscationResult.getObfuscatedCode()// 返回string混淆代码；
// obfuscationResult.getSourceMap()// 如果sourceMap启用选项 -如果选项设置为 ，则返回string源映射或空字符串；sourceMapModeinline
// console.log(obfuscationResult.getObfuscatedCode());

function comp(options, jsContent) {
    return JavaScriptObfuscator.obfuscate(
        jsContent, options
    );
}