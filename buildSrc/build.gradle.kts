plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.android.tools.build:gradle:7.3.1") // when changing, remember to change version in Versions.kt in buildSrc module
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.7.10") // when changing, remember to change version in Versions.kt in buildSrc module
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.21") // when changing, remember to change version in Versions.kt in buildSrc module
}