rootProject.name = "WalletConnect"

include(":showcase")
include(":core")

val wcModules = mapOf(
    "signSDK" to listOf("dapp", "samples_common", "wallet", "sign"),
    "chatSDK" to listOf("chat", "chatsample")
)

wcModules.forEach { (projectDirName, listOfModules) ->
    listOfModules.forEach { moduleName ->
        include(":$moduleName")
    }

    with(File(rootDir, projectDirName)) {
        listOfModules.forEach { moduleName ->
            project(":$moduleName").projectDir = resolve(moduleName)
        }
    }
}