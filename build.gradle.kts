import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "9.2.2"
    java
}

group = "com.github.mrjimin.nexoscreens"
version = providers.gradleProperty("project_version").getOrElse("2.0.1-26.2")

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.nexomc.com/releases")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    // Paper 26.2 uses the new 26.2.build.* version scheme and requires a JDK 25 toolchain.
    compileOnly("io.papermc.paper:paper-api:${providers.gradleProperty("paper_version").get()}")
    compileOnly("com.nexomc:nexo:${providers.gradleProperty("nexo_version").get()}") {
        isTransitive = false
    }
    compileOnly("net.dmulloy2:ProtocolLib:${providers.gradleProperty("protocollib_version").get()}")
    compileOnly("me.clip:placeholderapi:${providers.gradleProperty("placeholderapi_version").get()}")

    // Keep the plugin self-contained instead of downloading Kotlin at server startup.
    implementation(kotlin("stdlib"))
    implementation("org.bstats:bstats-bukkit:3.0.2")
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        // Paper/Leaf 26.2 runs on Java 25, but Java 21 bytecode keeps the plugin less restrictive.
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

val shadowJarPlugin = tasks.register<ShadowJar>("shadowJarPlugin") {
    archiveFileName.set("NexoScreens-${project.version}.jar")
    destinationDirectory.set(file("${project.rootDir}/target"))

    from(sourceSets.main.get().output)
    configurations = listOf(project.configurations.runtimeClasspath.get())

    // Prevent collisions with another plugin using bStats.
    relocate("org.bstats", "com.github.mrjimin.nexoscreens.libs.bstats")
}

tasks.named("build") {
    dependsOn(shadowJarPlugin)
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"

    // This project is a Paper/Leaf plugin. The old plugin.yml in the repository is stale.
    exclude("plugin.yml")

    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}