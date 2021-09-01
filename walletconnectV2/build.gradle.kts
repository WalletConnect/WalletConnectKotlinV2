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
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")

    implementation("com.tinder.scarlet:scarlet:0.1.12")
    implementation("com.tinder.scarlet:websocket-okhttp:0.1.12")
    implementation("com.tinder.scarlet:stream-adapter-coroutines:0.1.12")
    implementation("com.tinder.scarlet:message-adapter-moshi:0.1.12")
    implementation("com.squareup.moshi:moshi-adapters:1.12.0")

    implementation("org.json:json:20210307")

    testImplementation("com.tinder.scarlet:websocket-mockwebserver:0.1.12")
    testImplementation("com.tinder.scarlet:test-utils:0.1.12")

    testImplementation(platform("org.junit:junit-bom:5.7.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.5.21")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.1")

    testImplementation("io.mockk:mockk:1.12.0")
}