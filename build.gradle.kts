plugins {
    java
}

group = "com.github.mrjimin"
version = providers.gradleProperty("project_version").get()

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${providers.gradleProperty("paper_version").get()}")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.processResources {
    filteringCharset = "UTF-8"
}

tasks.jar {
    archiveFileName.set("NexoScreens-${project.version}.jar")
}
