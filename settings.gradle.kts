rootProject.name = "WalletConnect"

val excludedDirs = listOf(".idea", ".git", "build", ".gradle", ".github", "buildSrc", "gradle", "docs", "licenses")
val rootModules = listOf("showcase", "foundation")

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

//include(":showcase")

//val wcModules = mapOf(
//    "signSDK" to listOf("dapp", "samples_common", "wallet", "sign"),
//    "chatSDK" to listOf("chat", "chatsample")
//)
//
//wcModules.forEach { (projectDirName, listOfModules) ->
//    listOfModules.forEach { moduleName ->
//        include(":$moduleName")
//    }
//
//    with(File(rootDir, projectDirName)) {
//        listOfModules.forEach { moduleName ->
//            project(":$moduleName").projectDir = resolve(moduleName)
//        }
//    }
//}
//include(":Foundation")
