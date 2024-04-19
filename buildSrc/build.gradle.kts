plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

dependencies {
    implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}") // when changing, remember to change version in Versions.kt in buildSrc module
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}") // when changing, remember to change version in Versions.kt in buildSrc module
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:${libs.versions.dokka.get()}") // when changing, remember to change version in Versions.kt in buildSrc module
    implementation("com.google.firebase:firebase-appdistribution-gradle:${libs.versions.firebaseAppDistribution.get()}") // when changing, remember to change version in root build.gradle.kts

    testImplementation("junit:junit:${libs.versions.jUnit.get()}") // when changing, remember to change version in Versions.kt in buildSrc module
}