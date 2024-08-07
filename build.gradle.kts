plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "github.zimo"
//SNAPSHOT
version = "1.0.6"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
//    version.set("2022.2.5")
    version.set("2024.2")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.intellij.gradle"/* Plugin Dependencies */))
}

dependencies {
    // 声明对Kotlin标准库的依赖关系
    implementation(kotlin("test"))
    implementation("io.vertx:vertx-web:4.4.4")

    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
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

//        sinceBuild.set("223")
        sinceBuild.set("222")
//        untilBuild.set("241.*") // 2024.1
//        untilBuild.set("245.*") // 不指定idea 版本，包含预计所有版本
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
