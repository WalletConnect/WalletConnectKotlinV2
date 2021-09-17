import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    tasks.withType<KotlinCompile>() {
        kotlinOptions {
            sourceCompatibility = JavaVersion.VERSION_11.toString()
            targetCompatibility = JavaVersion.VERSION_11.toString()
            jvmTarget = JavaVersion.VERSION_11.toString()
            freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.time.ExperimentalTime"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    coroutines()
    moshi()
    scarlet()
    json()
    lazySodium()

    jUnit5()
    mockk()
}