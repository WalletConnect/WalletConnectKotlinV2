import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(libs.plugins.javaLibrary.get().pluginId)
    id(libs.plugins.kotlin.jvm.get().pluginId)
    alias(libs.plugins.google.ksp)
    id("publish-module-java")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = FOUNDATION
    extra[KEY_PUBLISH_VERSION] = FOUNDATION_VERSION
    extra[KEY_SDK_NAME] = "Foundation"
}

java {
    sourceCompatibility = jvmVersion
    targetCompatibility = jvmVersion
}

tasks.withType<KotlinCompile>() {
    kotlinOptions {
        jvmTarget = jvmVersion.toString()
    }
}

tasks.withType<Test> {
    systemProperty("SDK_VERSION", requireNotNull(project.extra.get(KEY_PUBLISH_VERSION)))
    systemProperty("TEST_RELAY_URL", System.getenv("TEST_RELAY_URL"))
    systemProperty("TEST_PROJECT_ID", System.getenv("TEST_PROJECT_ID"))
}

dependencies {
    api(libs.bundles.scarlet)
    api(platform(libs.okhttp.bom))
    api(libs.bundles.okhttp)
    implementation(libs.koin.jvm)
    api(libs.bundles.moshi)
    ksp(libs.moshi.ksp)
    api(libs.bouncyCastle)
    api(libs.mulitbase)

    testImplementation(libs.jerseyCommon)
    testImplementation(libs.coroutines.test)
}