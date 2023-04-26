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
    "walletconnectv2"
)
// TODO: Add to rootModules when new module is added to the project root directory
val rootModules = listOf("foundation")
val legacySampleModule = ":samples:legacy"

File(rootDir.path).listFiles { file -> file.isDirectory && file.name !in excludedDirs }?.forEach { childDir ->
    if (childDir.name !in rootModules) {
        childDir.listFiles { dir -> dir.isDirectory && dir.name !in excludedDirs }?.forEach { moduleDir ->
            val module = ":${moduleDir.parentFile.name}:${moduleDir.name}"

            if (module == legacySampleModule) {
                // Parse through legacy directory and add each sample app
                moduleDir.listFiles { dir -> dir.isDirectory && dir.name !in excludedDirs }?.forEach { sampleModuleDir ->
                    val sampleModule = "${module}:${sampleModuleDir.parentFile.name}:${sampleModuleDir.name}"
                    include(sampleModule)
                    project(sampleModule).projectDir = sampleModuleDir
                }
            } else {
                include(module)
                project(module).projectDir = moduleDir
            }
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