import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.incremental.deleteDirectoryContents
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Properties

var serverPort = 9317
plugins {
    kotlin("multiplatform") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    `maven-publish`
}


group = "zimoyin.github"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        compilations["main"].packageJson {
            customField("scripts", mapOf("babel" to "babel kotlin -d kotlin_babel", "build" to "webpack"))
        }
        generateTypeScriptDefinitions()
        useEsModules()
        nodejs()
        binaries.executable()
        taskList()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                // Babel dependencies
                implementation(devNpm("babel-loader", "^8.1.0"))
                implementation(devNpm("@babel/core", "^7.14.0"))
                implementation(devNpm("@babel/cli", "^7.14.0"))
                implementation(devNpm("@babel/preset-env", "^7.14.0"))

//                implementation(npm("@babel/plugin-transform-runtime", "^7.14.0"))
//                implementation(npm("@babel/plugin-proposal-class-properties", "^7.14.0"))
//                implementation(npm("@babel/plugin-transform-arrow-functions", "^7.14.0"))

                // Webpack dependencies
                implementation(devNpm("webpack", "^5.0.0"))
                implementation(devNpm("webpack-cli", "^4.0.0"))

                // javascript-obfuscator
                implementation(devNpm("javascript-obfuscator", "^4.1.1"))
                implementation(devNpm("webpack-obfuscator", "^3.5.1"))


                // Kotlin dependencies
                // Coroutines & serialization
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
            }
        }
    }
}

val configProperties = Properties().apply {
    load(File(rootDir, "./config/config.properties").inputStream())
}
val use_ui: String by configProperties
val webpack_intermediate_files: String by configProperties
val auto_upload: String by configProperties
val auto_execute: String by configProperties

fun KotlinJsTargetDsl.taskList() {

    tasks.register("httpRunProject") {
        group = "autojs"

        doLast {
            val postData = buildFile.parentFile.resolve("build/autojs/compilation").canonicalPath

            val connection: HttpURLConnection =
                URL("http://127.0.0.1:$serverPort/upload_run_path").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            // Send request
            val os = connection.outputStream
            os.write(postData.toByteArray())
            os.flush()
            os.close()

            // Get Response
            val responseCode = connection.responseCode
            println("Response Code : $responseCode")

            connection.disconnect()
        }
    }

    tasks.register("httpUploadProject") {
        group = "autojs"

        doLast {
            val postData = buildFile.parentFile.resolve("build/autojs/compilation").canonicalPath

            val connection: HttpURLConnection =
                URL("http://127.0.0.1:$serverPort/upload_path").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            // Send request
            val os = connection.outputStream
            os.write(postData.toByteArray())
            os.flush()
            os.close()

            // Get Response
            val responseCode = connection.responseCode
            println("Response Code : $responseCode")

            connection.disconnect()
        }
    }

    tasks.register("info") {
        group = "autojs"
        val nodeExecutable = compilations.getByName("main").npmProject.nodeJs.requireConfigured().nodeExecutable
        val nodeDir = compilations.getByName("main").npmProject.nodeJs.requireConfigured().nodeDir
        val nodeBinDir = compilations.getByName("main").npmProject.nodeJs.requireConfigured().nodeBinDir
        val platformName = compilations.getByName("main").npmProject.nodeJs.requireConfigured().platformName
        val architectureName = compilations.getByName("main").npmProject.nodeJs.requireConfigured().architectureName
        val path = compilations.getByName("main").npmProject.dir.path
        val mainPath = buildFile.parentFile.resolve("build/autojs")

        println("-----------------------------------------------------------------------------")
        println("> PlatformName name:       $platformName")
        println("> ArchitectureName name:   $architectureName")
        println("> Kotlin compiler version: ${KotlinVersion.CURRENT} ")
        println("> Kotlin compiler type:    ${KotlinJsCompilerType.IR}")
        println("> Node executable:         $nodeExecutable")
        println("> Node directory:          $nodeDir")
        println("> Node bin directory:      $nodeBinDir")
        println("> Build js project path:   $path")
        println("> Output path:             $mainPath")
        println("> Config:")
        println("   > use_ui                       :$use_ui")
        println("   > webpack_intermediate_files   :$use_ui")
        println("   > auto_upload                  :$use_ui")
        println("   > auto_execute                 :$use_ui")
        println("-----------------------------------------------------------------------------")
    }

    tasks.register("buildMainJs") {
        description = "Build project"
        group = "autojs"
        dependsOn("initEnvironment")
        dependsOn("compileIntermediateFiles")

        val path = compilations.getByName("main").npmProject.dir.path
        val mainPath = buildFile.parentFile.resolve("build/autojs/compilation")
        val mainJs = File(mainPath, "main.js")

        if (mainJs.exists() && auto_upload.contains("true")) {
            finalizedBy("httpUploadProject")
        }

        if (mainJs.exists() && auto_execute.contains("true")) {
            finalizedBy("httpRunProject")
        }
        doLast {

            exec {
                compilations.getByName("main").npmProject.useTool(
                    this, File(path, "..\\..\\node_modules\\webpack\\bin\\webpack.js").path, emptyList(), emptyList()
                )
            }

            if (mainPath.exists()) mainPath.deleteDirectoryContents()
            copy {
                from(File(path, "build/output"))
                into(mainPath)
            }

            File(path, "kotlin").listFiles()?.filter {
                it.name.endsWith(".js").not()
            }?.filter {
                it.name.endsWith(".mjs").not()
            }?.filter {
                it.name.endsWith(".ts").not()
            }?.filter {
                it.name.endsWith(".mjs.map").not()
            }?.forEach {
                copy {
                    println(it)
                    from(it)
                    into(mainPath)
                }
            }

            // main.js 前面添加 ui; 进入UI模式
            val mainJs = File(mainPath, "main.js")
            if (mainJs.exists() && use_ui.contains("true")) {
                val content = mainJs.readText()
                mainJs.writeText("\"ui\";\n$content")
            }
        }
    }


    tasks.register("webpack") {
        description = "Build project with webpack"
        group = "autojs"


        val path = compilations.getByName("main").npmProject.dir.path
        val mainPath = buildFile.parentFile.resolve("build/autojs/compilation")
        val sourceDir = File(path, "kotlin")
        val compilationDir = buildFile.parentFile.resolve("build/autojs/intermediate_compilation_files")
        val mainJs = File(mainPath, "main.js")

        if (mainJs.exists() && auto_upload.contains("true")) {
            finalizedBy("httpUploadProject")
        }

        if (mainJs.exists() && auto_execute.contains("true")) {
            finalizedBy("httpRunProject")
        }

        doFirst{
            // 复制中间编译文件回初次编译位置
            if (webpack_intermediate_files.contains("true")) {
                if (sourceDir.exists()) sourceDir.deleteDirectoryContents()
                copy {
                    from(compilationDir)
                    into(sourceDir)
                }
            }
            // 移动配置文件到 build 项目文件夹下
            val path = compilations.getByName("main").npmProject.dir.path
            val configPath = buildFile.parentFile.resolve("config")

            copy {
                from(configPath)
                into(File(path))
            }

            // 执行命令
            exec {
                compilations.getByName("main").npmProject.useTool(
                    this, File(path, "..\\..\\node_modules\\webpack\\bin\\webpack.js").path, emptyList(), emptyList()
                )
            }

            // 将编译后的文件复制到 mainPath
            if (mainPath.exists()) mainPath.deleteDirectoryContents()
            copy {
                from(File(path, "build/output"))
                into(mainPath)
            }

            // 将编译后的文件文件夹内的文件复制到 mainPath
            File(path, "kotlin").listFiles()?.filter {
                it.name.endsWith(".js").not()
            }?.filter {
                it.name.endsWith(".mjs").not()
            }?.filter {
                it.name.endsWith(".ts").not()
            }?.filter {
                it.name.endsWith(".mjs.map").not()
            }?.filter {
                it.name.endsWith(".js.map").not()
            }?.forEach {
                copy {
                    println(it)
                    from(it)
                    into(mainPath)
                }
            }

            // main.js 前面添加 "ui"; 进入UI模式
            if (mainJs.exists() && use_ui.contains("true")) {
                val content = mainJs.readText()
                mainJs.writeText("\"ui\";\n$content")
            }
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
            val path = compilations.getByName("main").npmProject.dir.path
            val configPath = buildFile.parentFile.resolve("config")

            copy {
                from(configPath)
                into(File(path))
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
            val path = compilations.getByName("main").npmProject.dir.path
            val configPath = buildFile.parentFile.resolve("config")

            copy {
                from(configPath)
                into(File(path))
            }
        }
    }

    tasks.register<Copy>("compileIntermediateFiles") {
        description = "Compile Autojs project"
        group = "autojs"
        dependsOn("jsPackageJson")
        dependsOn("jsDevelopmentExecutableCompileSync")
        dependsOn("compileDevelopmentExecutableKotlinJs")
        val sourceDir = File(compilations.getByName("main").npmProject.dir, "kotlin")
        val destDir = buildFile.parentFile.resolve("build/autojs/intermediate_compilation_files")

        from(sourceDir)
        into(destDir)

        doFirst {
            destDir.deleteDirectoryContents()
        }
    }
}