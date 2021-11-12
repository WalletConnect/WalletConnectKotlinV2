import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.DependencyHandlerScope

const val kotlinVersion = "1.5.31"
val jvmVersion = JavaVersion.VERSION_11

fun DependencyHandlerScope.scanner() {
    val mlKitBarcode = "16.0.1"
    "implementation"("com.google.mlkit:barcode-scanning:$mlKitBarcode")
    "implementation"("androidx.camera:camera-camera2:1.1.0-alpha01")
    "implementation"("androidx.camera:camera-lifecycle:1.1.0-alpha01")
    "implementation"("androidx.camera:camera-view:1.0.0-alpha21")
}

fun DependencyHandlerScope.ktxCore() {
    val ktxCoreVersion = "1.6.0"
    "implementation"("androidx.core:core-ktx:$ktxCoreVersion")
}

fun DependencyHandlerScope.lifecycle() {
    val lifecycleVersion = "2.3.1"
    "implementation"("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    "implementation"("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
}

fun DependencyHandlerScope.navigationComponent() {
    val navVersion = "2.3.5"
    "implementation"("androidx.navigation:navigation-fragment-ktx:$navVersion")
    "implementation"("androidx.navigation:navigation-ui-ktx:$navVersion")
}

fun DependencyHandlerScope.coroutines() {
    val coroutinesVersion = "1.5.1"
    "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    "testImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
}

fun DependencyHandlerScope.scarlet() {
    val scarletVersion = "0.1.12"
    "implementation"("com.tinder.scarlet:scarlet:$scarletVersion")
    "implementation"("com.tinder.scarlet:websocket-okhttp:$scarletVersion")
    "implementation"("com.tinder.scarlet:stream-adapter-coroutines:$scarletVersion")
    "implementation"("com.tinder.scarlet:message-adapter-moshi:$scarletVersion")
    "implementation"("com.tinder.scarlet:lifecycle-android:$scarletVersion")

    "testImplementation"("com.tinder.scarlet:websocket-mockwebserver:$scarletVersion")
    "testImplementation"("com.tinder.scarlet:test-utils:$scarletVersion")
}

fun DependencyHandlerScope.moshi() {
    val moshiVersion = "1.12.0"
    "implementation"("com.squareup.moshi:moshi-adapters:$moshiVersion")
    "implementation"("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    "kapt"("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")
}

fun DependencyHandlerScope.json() {
    val jsonVersion = "20210307"
    "implementation"("org.json:json:$jsonVersion")
}

fun DependencyHandlerScope.okhttp() {
    val okhttpVersion = "4.9.0"

    "implementation"(platform("com.squareup.okhttp3:okhttp-bom:$okhttpVersion"))
    "implementation"("com.squareup.okhttp3:okhttp")
    "implementation"("com.squareup.okhttp3:logging-interceptor")
}

fun DependencyHandlerScope.lazySodium() {
    val lazySodiumVersion = "5.0.2@aar"
    val jnaVersion = "5.8.0@aar"
    val slf4jVersion = "1.7.32"

    "implementation"("com.goterl:lazysodium-android:$lazySodiumVersion")
    "implementation"("net.java.dev.jna:jna:$jnaVersion")
    "testImplementation"("org.slf4j:slf4j-nop:$slf4jVersion")
}

fun DependencyHandlerScope.jUnit5() {
    val jUnit5Version = "5.7.2"

    "implementation"("androidx.test.ext:junit-ktx:1.1.3")

    "testImplementation"("androidx.test:core-ktx:1.4.0")
    "testImplementation"(platform("org.junit:junit-bom:$jUnit5Version"))
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:$jUnit5Version")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:$jUnit5Version")
    "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")

    "androidTestImplementation"("androidx.test:core-ktx:1.4.0")
    "androidTestImplementation"("androidx.test:runner:1.4.0")
    "androidTestImplementation"("androidx.test:rules:1.4.0")

    "androidTestImplementation"("org.junit.jupiter:junit-jupiter-api:$jUnit5Version")
    "androidTestImplementation"("de.mannodermaus.junit5:android-test-core:1.3.0")
    "androidTestRuntimeOnly"("de.mannodermaus.junit5:android-test-runner:1.3.0")
}

fun DependencyHandlerScope.robolectric() {
    val robolectricVersion = "4.6"

    "testImplementation" ("org.robolectric:robolectric:$robolectricVersion")
}

fun DependencyHandlerScope.mockk() {
    val mockkVersion = "1.12.0"

    "testImplementation"("io.mockk:mockk:$mockkVersion")
}

fun DependencyHandlerScope.timber() {
    val timberVersion = "5.0.1"

    "implementation"("com.jakewharton.timber:timber:$timberVersion")
}