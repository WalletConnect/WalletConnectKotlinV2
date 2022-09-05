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

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("SDK_VERSION", System.getenv("SDK_VERSION") ?: "2.0.0-rc.2") // todo: Automate versioning
    systemProperty("TEST_RELAY_URL", System.getenv("TEST_RELAY_URL"))
    systemProperty("TEST_PROJECT_ID", System.getenv("TEST_RELAY_URL"))
}

dependencies {
    scarlet()
    okhttp()
    koinJvm()
    moshi()
    moshiKsp()
    bouncyCastle()
    multibaseJava()
    restEasyJava()

    jUnit5()
    mockk()
}
