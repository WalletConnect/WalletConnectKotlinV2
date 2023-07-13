plugins {
    `java-platform`
    id("publish-module-java")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "android-bom"
    extra[KEY_PUBLISH_VERSION] = BOM_VERSION
    extra[KEY_SDK_NAME] = "Android BOM"
}

dependencies {
    constraints {
        api(project(":foundation"))
        api(project(":core:android"))
        api(project(":protocol:sign"))
        api(project(":protocol:auth"))
        api(project(":protocol:chat"))
        api(project(":protocol:push"))
//        api(project(":product:web3wallet"))
//        api(project(":product::web3inbox"))
//        api(project(":product::web3wallet"))
//        api(project(":product:walletconnectmodal"))
    }
}