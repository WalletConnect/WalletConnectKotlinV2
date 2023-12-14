rootProject.name = "WalletConnect"

val excludedDirs = listOf(
    ".qodana",
    "undefined",
    ".idea",
    ".git",
    "build",
    ".gradle",
    ".github",
    "buildSrc",
    "gradle",
    "docs",
    "licenses",
    "walletconnectv2"
)
// TODO: Add to rootModules when new module is added to the project root directory
val rootModules = listOf("foundation")

File(rootDir.path).listFiles { file -> file.isDirectory && file.name !in excludedDirs }?.forEach { childDir ->
    if (childDir.name !in rootModules) {
        childDir.listFiles { dir -> dir.isDirectory && dir.name !in excludedDirs}?.forEach { moduleDir ->
            val module = ":${moduleDir.parentFile.name}:${moduleDir.name}"
            include(module)
            project(module).projectDir = moduleDir
        }
    } else {
        include(":${childDir.name}")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}