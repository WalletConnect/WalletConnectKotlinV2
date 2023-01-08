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
        api(project(":androidCore:sdk"))
        api(project(":sign:sdk"))
        api(project(":auth:sdk"))
        api(project(":chat:sdk"))
        api(project(":web3:wallet"))
    }
}