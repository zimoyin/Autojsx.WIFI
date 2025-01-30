import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.incremental.deleteDirectoryContents
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

var serverPort = 9317
plugins {
    kotlin("multiplatform") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
}


group = "zimoyin.github"
version = "1.0-SNAPSHOT"
val sdkVersion = "2.1.3"

repositories {
    mavenCentral()
    maven {
        url = URI("https://jitpack.io")
    }
}

kotlin {
    checkSDK()
    js(IR) {
        // 输出模块名称
        moduleName = "main"
        // 设置package.json
        compilations["main"].packageJson {
            customField("scripts", mapOf("babel" to "babel kotlin -d kotlin_babel", "build" to "webpack"))
        }
//        generateTypeScriptDefinitions() // 生成 TypeScript 声明文件 (d.ts)
//        useEsModules() // 使用 ES 模块，使用后输出 mjs 文件。
        nodejs {
            testTask {
                // 是否启用测试
                enabled = true
            }
        }
        binaries.executable()
        taskList()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                // Babel dependencies (不使用 webpage 命令可以移除)
                implementation(devNpm("babel-loader", "^8.1.0"))
                implementation(devNpm("@babel/core", "^7.14.0"))
                implementation(devNpm("@babel/cli", "^7.14.0"))
                implementation(devNpm("@babel/preset-env", "^7.14.0"))

                // Webpack dependencies (不使用 webpage 命令可以移除)
                implementation(devNpm("webpack", "^5.0.0"))
                implementation(devNpm("webpack-cli", "^4.0.0"))

                // javascript-obfuscator (不使用 webpage 命令可以移除)
                implementation(devNpm("javascript-obfuscator", "^4.1.1"))
                implementation(devNpm("webpack-obfuscator", "^3.5.1"))


                // Kotlin dependencies
                // Coroutines & serialization (不使用可以移除，移除可能导致 SDK 出现异常)
//                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
                // 不可移除!
                implementation("com.github.zimoyin:autojs_kotlin_sdk:$sdkVersion")
            }
        }
    }
}


fun KotlinJsTargetDsl.taskList() {

    val name = moduleName ?: "main"
    val mainFile = compilations.getByName(name).npmProject.dir.get().asFile        // 编译输出文件夹
    val mainKotlinFile = File(mainFile, "kotlin")                                       // 编译输出文件夹
    val configFile = buildFile.parentFile.resolve("config")                         // 配置文件夹
    val compilationFile = buildFile.parentFile.resolve("build/autojs/compilation") // 最后编译输出文件夹
    val intermediateCompilationFile =
        buildFile.parentFile.resolve("build/autojs/intermediate_compilation_files") // 最后中间编译输出文件夹
    val intermediateCompilationMainJsFile = File(intermediateCompilationFile, "main.js") // 最后中间编译输出文件夹
    val compilationMainJsFile = File(compilationFile, "main.js")                                    // 输出最后编译文件
    val processedResourcesFile = buildFile.parentFile.resolve("build/processedResources/js/$name")

    val webpackIntermediateFiles = project.findProperty("autojs.webpack.intermediate.files") as String
    val useUI = project.findProperty("autojs.use.ui") as String
    val webpackAutoRun = project.findProperty("autojs.webpack.auto.run") as String
    val webpackAutoUpload = project.findProperty("autojs.webpack.auto.upload") as String
    val webpackPreRunCompose = project.findProperty("autojs.webpack.pre.run.compose") as String
    val isDownloadNode = (project.findProperty("kotlin.js.node.download") as String).contains("true")

    // 是否使用 nodejs 的下载功能.让 gradle 去自动下载 node 到 gradle 缓存文件夹
    rootProject.plugins.withType<NodeJsRootPlugin> {
        rootProject.the<NodeJsRootExtension>().download = isDownloadNode
    }

    tasks.register("run") {
        group = "autojs"
        dependsOn("compile")
        finalizedBy("httpRunProject")

        doLast {
            // 将编译后的文件复制到 mainPath
            if (compilationFile.exists()) compilationFile.deleteDirectoryContents()
            copy {
                from(intermediateCompilationFile)
                into(compilationFile)
            }
            // main.js 前面添加 "ui"; 进入UI模式
            if (compilationMainJsFile.exists() && useUI.contains("true")) {
                val content = compilationMainJsFile.readText()
                val newlineIndex1 = content.indexOf("\r\n")
                val newlineIndex2 = content.indexOf('\n')
                var firstLine = if (newlineIndex1 == -1) null else content.substring(0, newlineIndex1)
                firstLine = firstLine ?: if (newlineIndex2 == -1) "" else content.substring(0, newlineIndex2)
                if (!firstLine.contains("ui") || firstLine.length > 6) {
                    compilationMainJsFile.writeText("\"ui\";\n$content")
                }
            }
        }
    }


    tasks.register("httpRunProject") {
        group = "autojs"
        doLast {
            val isEmpty = compilationFile.list().let {
                it?.isEmpty() ?: true
            }
            if (!compilationFile.exists() && isEmpty) {
                throw NullPointerException("${compilationFile.canonicalPath} path is null")
            }
            http("http://127.0.0.1:$serverPort/upload_run_path", compilationFile.canonicalPath)
        }
    }

    tasks.register("httpUploadProject") {
        group = "autojs"
        doLast {
            val isEmpty = compilationFile.list().let {
                it?.isEmpty() ?: true
            }
            if (!compilationFile.exists() && isEmpty) {
                throw NullPointerException("${compilationFile.canonicalPath} path is null")
            }
            http("http://127.0.0.1:$serverPort/upload_path", compilationFile.canonicalPath)
        }
    }

    tasks.register("webpack") {
        description = "Build project with webpack"
        group = "autojs"

        if (webpackPreRunCompose.contains("true")) {
            dependsOn("compile")
        }
        if (compilationMainJsFile.exists() && webpackAutoUpload.contains("true")) {
            finalizedBy("httpUploadProject")
        }

        if (compilationMainJsFile.exists() && webpackAutoRun.contains("true")) {
            finalizedBy("httpRunProject")
        }

        doLast {
            val path = mainKotlinFile.path
            // 复制中间编译文件回初次编译位置
            if (webpackIntermediateFiles.contains("true")) {
                if (mainKotlinFile.exists()) mainKotlinFile.deleteDirectoryContents()
                copy {
                    from(intermediateCompilationFile)
                    into(mainKotlinFile)
                }
            }
            // 移动配置文件到 build 项目文件夹下
            copy {
                from(configFile)
                into(mainFile)
            }

            // 执行命令
            exec {
                compilations.getByName("main").npmProject.useTool(
                    this,
                    File(path, "..\\..\\..\\node_modules\\webpack\\bin\\webpack.js").path,
                    emptyList(),
                    emptyList()
                )
            }


            // 将编译后的文件复制到 mainPath
            if (compilationFile.exists()) {
                compilationFile.deleteDirectoryContents()
                copy {
                    from(mainFile.resolve("dist"))
                    into(compilationFile)
                }
            }

            // 复制资源文件到 mainPath
            if (processedResourcesFile.exists()) {
                copy {
                    from(processedResourcesFile)
                    into(compilationFile)
                }
            }

            // main.js 前面添加 "ui"; 进入UI模式
            if (compilationMainJsFile.exists() && useUI.contains("true")) {
                val content = compilationMainJsFile.readText()
                compilationMainJsFile.writeText("\"ui\";\n$content")
            }
        }
    }

    tasks.register("info") {
        group = "autojs"
        rootProject.plugins.withType<NodeJsRootPlugin> {
            val nodeJsRootExtension = rootProject.the<NodeJsRootExtension>()

            val nodeExecutable = compilations.getByName("main").npmProject.nodeJs.requireConfigured().executable
            val nodeDir = compilations.getByName("main").npmProject.nodeJs.requireConfigured().nodeBinDir
            val platformName = compilations.getByName("main").npmProject.nodeJs.requireConfigured().platformName
            val architectureName = compilations.getByName("main").npmProject.nodeJs.requireConfigured().architectureName
            val path = mainFile.path
            val mainPath = buildFile.parentFile.resolve("build/autojs")

            val httpProxyHost = project.findProperty("systemProp.http.proxyHost") as String?
            val httpProxyPort = project.findProperty("systemProp.http.proxyPort") as String?
            val httpsProxyHost = project.findProperty("systemProp.https.proxyHost") as String?
            val httpsProxyPort = project.findProperty("systemProp.https.proxyPort") as String?

            println("-----------------------------------------------------------------------------")
            println("> PlatformName name:       $platformName")
            println("> ArchitectureName name:   $architectureName")
            println("> Kotlin compiler version: ${KotlinVersion.CURRENT} ")
            println("> Kotlin compiler type:    ${KotlinJsCompilerType.IR}")
            println("> Node executable:         $nodeExecutable")
            println("> Node directory:          $nodeDir")
            println("> Build js project path:   $path")
            println("> Is Node Download:        ${nodeJsRootExtension.download}")
            println("> Node downloadBaseUrl:    ${nodeJsRootExtension.downloadBaseUrl}")
            println("> Node installationDir:    ${nodeJsRootExtension.installationDir}")
            println("> Output path:             $mainPath")
            println("> Config:")
            println("   > use_ui                       :$useUI")
            println("   > webpack_intermediate_files   :$webpackIntermediateFiles")
            println("   > http proxy                   :${httpProxyHost ?: ""}:$httpProxyPort")
            println("   > https proxy                  :${httpsProxyHost ?: ""}:$httpsProxyPort")
            println("-----------------------------------------------------------------------------")
        }
    }

    tasks.register("initEnvironment") {
        description = "Initialize environment"
        group = "autojs"
        // 设置 Node.js 环境(下载并安装 Node.js,配置 Node.js 的版本,安装 Yarn 包管理工具)
        dependsOn("kotlinNodeJsSetup")
        // 生成 package.json 文件
        dependsOn("jsPackageJson")
        // 升级 yarn.lock 文件
        dependsOn("kotlinUpgradeYarnLock")

        doLast {
            // 移动配置文件到 build 项目文件夹下
            copy {
                from(configFile)
                into(mainFile)
            }
        }
    }

    tasks.register("npmInstall") {
        description = "Update environment"
        group = "autojs"
        // 生成 package.json 文件
        dependsOn("jsPackageJson")
        // 升级 yarn.lock 文件
        dependsOn("kotlinUpgradeYarnLock")
        // 安装依赖包
        dependsOn("kotlinNpmInstall")

        doLast {
            // 移动配置文件到 build 项目文件夹下
            copy {
                from(buildFile.parentFile.resolve("config"))
                into(mainFile)
            }
        }
    }

    tasks.register("compile") {
        description = "Compile Autojs project"
        group = "autojs"
        dependsOn("initEnvironment")
        dependsOn("jsPackageJson")
        // 直接编译发布可运行 js 文件
        dependsOn("jsProductionExecutableCompileSync")
        dependsOn("compileProductionExecutableKotlinJs")

        doFirst {
            if (intermediateCompilationFile.exists()) intermediateCompilationFile.deleteDirectoryContents()
            if (compilationFile.exists()) compilationFile.deleteDirectoryContents()
        }

        doLast {
            copy {
                from(mainKotlinFile)
                into(intermediateCompilationFile)
            }
            // main.js 前面添加 "ui"; 进入UI模式
            if (intermediateCompilationMainJsFile.exists() && useUI.contains("true")) {
                val content = intermediateCompilationMainJsFile.readText()
                intermediateCompilationMainJsFile.writeText("\"ui\";\n$content")
            }
        }
    }

    tasks.register("compileContinuous") {
        dependsOn("jsPackageJson")
        dependsOn("jsProductionExecutableCompileSync")
        dependsOn("compileProductionExecutableKotlinJs")
    }
}

fun http(url: String, data: String?, method: String = "POST"): String {
    val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
    println("send data: $data")
    connection.requestMethod = method
    connection.doOutput = true
    connection.setRequestProperty("Content-Type", "application/json")

    // Send request
    if (data != null) {
        connection.outputStream.use { os ->
            os.write(data.toByteArray())
            os.flush()
            os.close()
        }
    }

    // Get Response
    val responseCode = connection.responseCode
    println("Response Code : $responseCode")
    if (responseCode != 200) {
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        throw Exception("Response Code : $responseCode, Response: $response")
    }

    // 接收值
    val response = connection.inputStream.bufferedReader().use {
        it.readLine()
    }


    connection.disconnect()
    return response
}

fun checkSDK() {
    val url = "https://jitpack.io/api/builds/com.github.zimoyin/autojs_kotlin_sdk/latestOk"
    try {
        val resp = http(url, null, "GET")
        val newVersion = Gson().fromJson(resp, JsonObject::class.java).get("version").asString
        newVersion.replace(".","").toInt()
        if (newVersion != sdkVersion) {
            System.err.println(">>>>> sdk new version: $newVersion")
            System.err.println(">>>>> sdk old version: $sdkVersion")
            throw IllegalStateException("SDK version too low: $sdkVersion, Please update SDK to $newVersion")
        }
        println(">>>>> sdk version: $sdkVersion")
    } catch (e:IllegalStateException){
        System.err.println("${e::class.java.simpleName}: "+e.message)
    }catch (e: Exception) {
        System.err.println("${e::class.java.simpleName}: "+e.message)
    }
}