// [x] Wszystko ładnie ma działać za odpaleniem jednego skryptu
// [ ] Udokumentować co jeszcze trzeba zrobić żeby łądnie działało w CI
// [x] Dodać testy -> kod wywalić z tego pliku do osobnego pliku i testować
// [x] zmienić readme aby miało - zamist kropki w czacie
// [ ] ogarnać jak zrobić release z GHA


tasks {
    val properties = project.properties as Map<String, Any>

    // Example usage:
    // ./gradlew versionBump -Ptype=fix
    // ./gradlew versionBump -Ptype=release
    register("versionBump", Exec::class) {
        val scriptFilePath = CHECK_MODULES_SCRIPT_PATH
        val outputFilePath = CHECK_MODULES_OUTPUT_PATH
        val bumpType = getBumpType(properties)
        if (bumpType == VersionBumpType.MANUAL) throw Throwable("Unsupported bump type: $bumpType. Please use manualBump task instead.")

        // Run modules changes check script
        commandLine("sh", scriptFilePath, outputFilePath)

        doLast {
            val propertiesWithChangedModules = parseChangedModules(properties)

            when (bumpType) {
                VersionBumpType.FIX -> writeFiles(bumpVersions(propertiesWithChangedModules, VersionBumpType.FIX, InputType.AUTOMATIC))
                VersionBumpType.RELEASE -> writeFiles(bumpVersions(propertiesWithChangedModules, VersionBumpType.RELEASE, InputType.AUTOMATIC))
                else -> {}
            }
        }
    }

    // Example usage:
    // ./gradlew manualBump -PBOM=1.0.0 -PFOUNDATION=1.0.0 -PCORE=1.0.0 -PSIGN=1.0.0 -PAUTH=1.0.0 -PCHAT=1.0.0 -PNOTIFY=1.0.0 -PWEB_3_WALLET=1.0.0 -PWEB_3_MODAL=1.0.0 -PWC_MODAL=1.0.0 -PMODAL_CORE=1.0.0
    // ./gradlew manualBump -PNOTIFY=2.0.0
    register("manualBump") {
        doLast {
            writeFiles(bumpVersions(properties, VersionBumpType.MANUAL, InputType.MANUAL))
        }
    }

    // Example usage:
    // ./gradlew releaseBump -Pmodules=FOUNDATION,CORE,SIGN,AUTH,CHAT,NOTIFY,WEB_3_WALLET,WEB_3_MODAL,WC_MODAL,MODAL_CORE
    // ./gradlew releaseBump -Pmodules=FOUNDATION
    // ./gradlew releaseBump -Pmodules=AUTH,WEB_3_MODAL
    register("releaseBump") {
        doLast {
            writeFiles(bumpVersions(properties, VersionBumpType.RELEASE, InputType.AUTOMATIC))
        }
    }

    // Example usage:
    // ./gradlew fixBump -Pmodules=FOUNDATION,CORE,SIGN,AUTH,CHAT,NOTIFY,WEB_3_WALLET,WEB_3_MODAL,WC_MODAL,MODAL_CORE
    // ./gradlew fixBump -Pmodules=FOUNDATION
    // ./gradlew fixBump -Pmodules=AUTH,WEB_3_MODAL
    register("fixBump") {
        doLast {
            writeFiles(bumpVersions(properties, VersionBumpType.FIX, InputType.AUTOMATIC))
        }
    }
}
