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
        api(project(":core:modal"))
        api(project(":protocol:sign"))
        api(project(":protocol:auth"))
        api(project(":protocol:chat"))
        api(project(":protocol:notify"))
        api(project(":product:walletconnectmodal"))
//        api(project(":product:web3modal"))    TODO: Add back in once web3modal is ready
        api(project(":product:web3inbox"))
        api(project(":product:web3wallet"))
    }
}