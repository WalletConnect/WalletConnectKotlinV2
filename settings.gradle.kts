rootProject.name = "WalletConnect"

val excludedDirs = listOf(".idea", ".git", "build", ".gradle", ".github", "buildSrc", "gradle", "docs", "licenses")
// TODO: Add to rootModules when new module is added to the project root directory
val rootModules = listOf("showcase", "foundation", /*"android_core_impl", "android_core_api"*/)

File(rootDir.path).listFiles { file -> file.isDirectory && file.name !in excludedDirs }?.forEach { childDir ->
    if (childDir.name !in rootModules) {
        childDir.listFiles { dir -> dir.isDirectory }?.forEach { moduleDir ->
            val module = ":${moduleDir.parentFile.name}:${moduleDir.name}"
            include(module)
            project(module).projectDir = moduleDir
        }
    } else {
        include(":${childDir.name}")
    }
}