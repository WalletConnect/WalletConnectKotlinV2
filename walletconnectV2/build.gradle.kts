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

    implementation("io.ktor:ktor-client-websockets:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")

//    testImplementation(kotlin("test"))
    testImplementation(platform("org.junit:junit-bom:5.7.2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.5.21")


    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.1")

    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")

    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.1")

    testImplementation("io.mockk:mockk:1.12.0")
}