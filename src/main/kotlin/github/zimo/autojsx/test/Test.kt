package github.zimo.autojsx.test
import com.google.javascript.jscomp.CompilationLevel
import com.google.javascript.jscomp.Compiler
import com.google.javascript.jscomp.CompilerOptions
import com.google.javascript.jscomp.SourceFile
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 压缩与初步混淆
 */
fun main() {

    val compiler = Compiler()

    val options = CompilerOptions().apply {
//        setWarningLevel(DiagnosticGroups.UNDEFINED_VARIABLES, CheckLevel.OFF)
//        setWarningLevel(DiagnosticGroups.UNDEFINED_VARIABLES, CheckLevel.OFF)
//        setWarningLevel(DiagnosticGroups.CHECK_VARIABLES, CheckLevel.OFF)
    }
    // BUNDLE简单地对文件进行排序并将其连接到输出。
//    CompilationLevel.BUNDLE.setOptionsForCompilationLevel(options)
    // 只是简单的去除空格换行注释。
//    CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options)'
    // 比Whitespace only更高端一点，在其基础上，还对局部变量的变量名进行缩短。还会去除死代码
    CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options)
    // 比Whitespace only更高端一点，在其基础上，还对局部变量的变量名进行缩短。这也是其他压缩工具所使用的压缩方式，如UglifyJS等，也是最为主流的压缩方式。比较安全。
//    CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(options)


    // To get the complete set of externs, the logic in
    // CompilerRunner.getDefaultExterns() should be used here.
    val extern: SourceFile = SourceFile.fromCode(
        "externs.js", ""
    )


    // The dummy input name "input.js" is used here so that any warnings or
    // errors will cite line numbers in terms of input.js.
    val jsFile: SourceFile = SourceFile.fromCode(
        "input.js",
        "function s(){${Files.readString(Paths.get("./test.js"))}}; s()"
    )


    // compile() returns a Result, but it is not needed here.
    compiler.compile(extern, jsFile, options)


    // The compiler is responsible for generating the compiled code; it is not
    // accessible via the Result.
    if (compiler.errorCount > 0) {
        val erroInfo = StringBuilder()
        for (jsError in compiler.getErrors()) {
            erroInfo.append(jsError.toString())
        }
        println(erroInfo)
    }
//    return compiler.toSource()
    println(compiler.toSource())
}