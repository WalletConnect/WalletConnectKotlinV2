plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

dependencies {
//    Gradle plugin
    implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:${libs.versions.dokka.get()}")
    implementation("com.google.firebase:firebase-appdistribution-gradle:${libs.versions.firebaseAppDistribution.get()}")

    testImplementation("junit:junit:${libs.versions.jUnit.get()}")
}