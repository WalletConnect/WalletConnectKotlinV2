import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.DependencyHandlerScope

const val kotlinVersion = "1.7.10"
const val kspVersion = "1.7.10-1.0.6"
val jvmVersion = JavaVersion.VERSION_11
const val sqlDelightVersion = "1.5.2"
const val MIN_SDK: Int = 23

fun DependencyHandlerScope.scanner() {
    val mlKitBarcode = "16.0.1"
    "implementation"("com.google.mlkit:barcode-scanning:$mlKitBarcode")
    "implementation"("androidx.camera:camera-camera2:1.1.0-alpha01")
    "implementation"("androidx.camera:camera-lifecycle:1.1.0-alpha01")
    "implementation"("androidx.camera:camera-view:1.0.0-alpha21")
}

fun DependencyHandlerScope.ktxCore() {
    val ktxCoreVersion = "1.8.0"
    "implementation"("androidx.core:core-ktx:$ktxCoreVersion")
}

fun DependencyHandlerScope.lifecycle() {
    val lifecycleVersion = "2.3.1"
    "implementation"("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    "implementation"("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    "implementation"("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
}

fun DependencyHandlerScope.navigationComponent() {
    val navVersion = "2.4.1"
    "api"("androidx.navigation:navigation-fragment-ktx:$navVersion")
    "api"("androidx.navigation:navigation-ui-ktx:$navVersion")
}

fun DependencyHandlerScope.coroutines() {
    val coroutinesVersion = "1.6.0"
    "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    "testImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
}

fun DependencyHandlerScope.scarlet(includeAndroid: Boolean = true) {
    val scarletVersion = "1.0.0"
    val scarletPackage = "com.github.WalletConnect.Scarlet"
    "implementation"("$scarletPackage:scarlet:$scarletVersion")
    "implementation"("$scarletPackage:websocket-okhttp:$scarletVersion")
    "implementation"("$scarletPackage:stream-adapter-coroutines:$scarletVersion")
    "implementation"("$scarletPackage:message-adapter-moshi:$scarletVersion")

    if (includeAndroid) {
        "implementation"("$scarletPackage:lifecycle-android:$scarletVersion")
    }

    "testImplementation"("$scarletPackage:websocket-mockwebserver:$scarletVersion")
    "testImplementation"("$scarletPackage:test-utils:$scarletVersion")
}


fun DependencyHandlerScope.retrofit() {
    val retrofitVersion = "2.9.0"
    "implementation"("com.squareup.retrofit2:retrofit:$retrofitVersion")
    "implementation"("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
}


fun DependencyHandlerScope.moshi_N_ksp() {
    val moshiVersion = "1.13.0"
    "implementation"("com.squareup.moshi:moshi-adapters:$moshiVersion")
    "implementation"("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    "ksp"("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")
}

fun DependencyHandlerScope.okhttp() {
    val okhttpVersion = "4.9.0"

    "implementation"(platform("com.squareup.okhttp3:okhttp-bom:$okhttpVersion"))
    "implementation"("com.squareup.okhttp3:okhttp")
    "implementation"("com.squareup.okhttp3:logging-interceptor")
}

fun DependencyHandlerScope.bouncyCastle() {
    val bouncyCastleVersion = "1.70"
    "implementation"("org.bouncycastle:bcprov-jdk15on:$bouncyCastleVersion")
}

fun DependencyHandlerScope.sqlDelight() {
    val sqlCipherVersion = "4.5.0@aar"
    "implementation"("com.squareup.sqldelight:android-driver:$sqlDelightVersion")
    "implementation"("com.squareup.sqldelight:coroutines-extensions-jvm:$sqlDelightVersion")
    "implementation"("net.zetetic:android-database-sqlcipher:$sqlCipherVersion")

    "testImplementation"("com.squareup.sqldelight:sqlite-driver:$sqlDelightVersion")
    "testImplementation"("org.xerial:sqlite-jdbc:3.8.10.2") {
        // Override the version of sqlite used by sqlite-driver to match Android API 23
        isForce = true
    }
}

fun DependencyHandlerScope.koin(includeAndroid: Boolean = true) {
    val koinVersion = "3.2.0"

    if (includeAndroid) {
        "api"("io.insert-koin:koin-android:$koinVersion")
    } else {
        "implementation"("io.insert-koin:koin-core:$koinVersion")
        "testImplementation"("io.insert-koin:koin-test:$koinVersion")
    }
}

fun DependencyHandlerScope.glide_N_kapt() {
    "implementation"("com.github.bumptech.glide:glide:4.12.0")
    "kapt"("com.github.bumptech.glide:compiler:4.12.0")
}

fun DependencyHandlerScope.multibaseJava() {
    val multibaseVersion = "1.1.0"

    "implementation"("com.github.multiformats:java-multibase:$multibaseVersion") //https://mvnrepository.com/artifact/com.github.multiformats/java-multibase/1.1.0 vulnerability detected with library
}

fun DependencyHandlerScope.jUnit5() {
    val jUnit5Version = "5.7.2"

    "testImplementation"(platform("org.junit:junit-bom:$jUnit5Version"))
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:$jUnit5Version")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:$jUnit5Version")
    "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")

    "androidTestImplementation"("org.junit.jupiter:junit-jupiter-api:$jUnit5Version")
    "androidTestImplementation"("de.mannodermaus.junit5:android-test-core:1.3.0")
    "androidTestRuntimeOnly"("de.mannodermaus.junit5:android-test-runner:1.3.0")
}

fun DependencyHandlerScope.androidXTest() {
    val androidxTestVersion = "1.4.0"

    "testImplementation"("androidx.test.ext:junit-ktx:1.1.3")
    "testImplementation"("androidx.test:core-ktx:$androidxTestVersion")

    "androidTestImplementation"("androidx.test:core-ktx:$androidxTestVersion")
    "androidTestImplementation"("androidx.test:runner:$androidxTestVersion")
    "androidTestImplementation"("androidx.test:rules:$androidxTestVersion")
}

fun DependencyHandlerScope.robolectric() {
    val robolectricVersion = "4.6"

    "testImplementation"("org.robolectric:robolectric:$robolectricVersion")
}

fun DependencyHandlerScope.mockk() {
    val mockkVersion = "1.12.0"

    "testImplementation"("io.mockk:mockk:$mockkVersion")
}

fun DependencyHandlerScope.testJson() {
    val jsonVersion = "20220320"
    "testImplementation"("org.json:json:$jsonVersion")
}

fun DependencyHandlerScope.timber() {
    val timberVersion = "5.0.1"

    "implementation"("com.jakewharton.timber:timber:$timberVersion")
}

fun DependencyHandlerScope.security() {
    val androidSecurityVersion = "1.0.0"
    "implementation"("androidx.security:security-crypto:$androidSecurityVersion")
}
