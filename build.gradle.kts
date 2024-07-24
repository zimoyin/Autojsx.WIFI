plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "github.zimo"
//SNAPSHOT
version = "1.0.4"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
//    version.set("2022.2.5")
    version.set("2024.1.4")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))
}

dependencies {
    // 声明对Kotlin标准库的依赖关系
    implementation(kotlin("test"))
//    implementation("com.google.javascript:closure-compiler:v20230502")
    // https://mvnrepository.com/artifact/io.vertx/vertx-web
    implementation("io.vertx:vertx-web:4.4.4")
    // https://mvnrepository.com/artifact/io.vertx/vertx-lang-kotlin
    implementation("io.vertx:vertx-lang-kotlin:4.4.4")
    implementation("com.caoccao.javet:javet:3.0.1") // Linux and Windows (x86_64)
//    implementation("com.caoccao.javet:javet-linux-arm64:3.0.1") // Linux (arm64)
//    implementation("com.caoccao.javet:javet-macos:3.0.1") // Mac OS (x86_64 and arm64)
//    implementation("com.caoccao.javet:javet-android:3.0.1") // Android (arm, arm64, x86 and x86_64)

}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
//        sinceBuild.set("222")
//        untilBuild.set("232.*")

//        sinceBuild.set("222") // 2022
        sinceBuild.set("223") // 2022.3
        untilBuild.set("241.*") // 2024
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
