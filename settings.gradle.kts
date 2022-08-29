rootProject.name = "WalletConnect"

val excludedDirs = listOf(".idea", ".git", "build", ".gradle", ".github", "buildSrc", "gradle", "docs", "licenses")
// TODO: Comment when to use this
val rootModules = listOf("showcase", "foundation", "android_core_impl")

File(rootDir.path).listFiles { file -> file.isDirectory && file.name !in excludedDirs }?.forEach { childDir ->
    if (childDir.name !in rootModules) {
        childDir.listFiles { dir -> dir.isDirectory }?.forEach { moduleDir ->
            include(":${moduleDir.name}")
            project(":${moduleDir.name}").projectDir = moduleDir
        }
    } else {
        include(":${childDir.name}")
    }
}