rootProject.name = "WalletConnect"

val excludedDirs = listOf(
    ".idea",
    ".git",
    "build",
    ".gradle",
    ".github",
    "buildSrc",
    "gradle",
    "docs",
    "licenses",
    "common",
    "signSDK",
    "chatSDK",
    "authSDK",
    "coreSDK",
    "web3wallet",
    "web3inbox",
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