plugins {
    `java-platform`
    id("publish-module-java")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "android-bom"
    extra[KEY_PUBLISH_VERSION] = "1.0.0"
    extra[KEY_SDK_NAME] = "Android BOM"
}

dependencies {
    constraints {
        api(project(":foundation"))
        api(project(":androidCore:sdk"))
        api(project(":androidCore:impl"))
        api(project(":sign:sdk"))
        api(project(":auth:sdk"))
        api(project(":chat:sdk"))
    }
}