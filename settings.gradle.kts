rootProject.name = "WalletConnect"

val signModules = mapOf("sign" to listOf("dapp", "samples_common", "wallet", "walletconnectv2"))
val chatModules = mapOf("chat" to emptyList<String>())

(signModules /*+ chatModules*/).forEach { (projectDirName, listOfModules) ->
    listOfModules.forEach { moduleName ->
        include(":$moduleName")
    }

    with(File(rootDir, projectDirName)) {
        listOfModules.forEach { moduleName ->
            project(":$moduleName").projectDir = resolve(moduleName)
        }
    }
}