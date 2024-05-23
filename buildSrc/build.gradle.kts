plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

dependencies {
//    Gradle plugin
    implementation("com.android.tools.build:gradle:8.4.1")//${libs.versions.agp.get()}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")//${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:${libs.versions.dokka.get()}")
    implementation("com.google.firebase:firebase-appdistribution-gradle:${libs.versions.firebaseAppDistribution.get()}")

    testImplementation("junit:junit:${libs.versions.jUnit.get()}")
}