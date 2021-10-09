import org.gradle.kotlin.dsl.DependencyHandlerScope

const val kotlinVersion = "1.5.31"

fun DependencyHandlerScope.coroutines() {
    val coroutinesVersion = "1.5.1"
    "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    "testImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    "intTestImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
}

fun DependencyHandlerScope.scarlet() {
    val scarletVersion = "0.1.12"
    "implementation"("com.tinder.scarlet:scarlet:$scarletVersion")
    "implementation"("com.tinder.scarlet:websocket-okhttp:$scarletVersion")
    "implementation"("com.tinder.scarlet:stream-adapter-coroutines:$scarletVersion")
    "implementation"("com.tinder.scarlet:message-adapter-moshi:$scarletVersion")

    "testImplementation"("com.tinder.scarlet:websocket-mockwebserver:$scarletVersion")
    "testImplementation"("com.tinder.scarlet:test-utils:$scarletVersion")
    "intTestImplementation"("com.tinder.scarlet:websocket-mockwebserver:$scarletVersion")
    "intTestImplementation"("com.tinder.scarlet:test-utils:$scarletVersion")
}

fun DependencyHandlerScope.moshi() {
    val moshiVersion = "1.12.0"
    "implementation"("com.squareup.moshi:moshi-adapters:$moshiVersion")
}

fun DependencyHandlerScope.json() {
    val jsonVersion = "20210307"
    "implementation"("org.json:json:$jsonVersion")
}

fun DependencyHandlerScope.lazySodium() {
    val lazySodiumVersion = "5.1.1"
    val jnaVersion = "5.9.0"
    val slf4jVersion = "1.7.32"

    "implementation"("com.goterl:lazysodium-java:$lazySodiumVersion")
    "implementation"("net.java.dev.jna:jna:$jnaVersion")

    "testImplementation"("org.slf4j:slf4j-nop:$slf4jVersion")
    "intTestImplementation"("org.slf4j:slf4j-nop:$slf4jVersion")
}

fun DependencyHandlerScope.jUnit5() {
    val jUnit5Version = "5.7.2"

    "testImplementation"(platform("org.junit:junit-bom:$jUnit5Version"))
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:$jUnit5Version")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:$jUnit5Version")
    "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")

    "intTestImplementation"(platform("org.junit:junit-bom:$jUnit5Version"))
    "intTestImplementation"("org.junit.jupiter:junit-jupiter-api:$jUnit5Version")
    "intTestRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:$jUnit5Version")
    "intTestImplementation"("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")
}

fun DependencyHandlerScope.mockk() {
    val mockkVersion = "1.12.0"

    "testImplementation"("io.mockk:mockk:$mockkVersion")
    "intTestImplementation"("io.mockk:mockk:$mockkVersion")
}