rootProject.name = "WalletConnect Kotlin"

val excludedDirs = listOf(
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
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
        jcenter() // Warning: this repository is going to shut down soon
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}