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
    implementation("com.android.tools.build:gradle:8.0.2") // when changing, remember to change version in Versions.kt in buildSrc module
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21") // when changing, remember to change version in Versions.kt in buildSrc module
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.7.10") // when changing, remember to change version in Versions.kt in buildSrc module
    implementation("com.google.firebase:firebase-appdistribution-gradle:4.0.0") // when changing, remember to change version in root build.gradle.kts

    testImplementation("junit:junit:4.13.2") // when changing, remember to change version in Versions.kt in buildSrc module
}