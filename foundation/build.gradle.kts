import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    kotlin("jvm")
    id("com.google.devtools.ksp") version kspVersion
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

dependencies {
    scarlet(includeAndroid = false)
    okhttp()
    koin(includeAndroid = false)
    moshi_N_ksp()
}
