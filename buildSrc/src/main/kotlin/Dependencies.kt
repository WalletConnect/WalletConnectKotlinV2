import org.gradle.kotlin.dsl.DependencyHandlerScope

const val kotlinVersion = "1.6.10"
val jvmVersion = JavaVersion.VERSION_11
const val sqlDelightVersion = "1.5.2"
const val MIN_SDK: Int = 23
const val dokkaVersion = "1.6.21"

fun DependencyHandlerScope.scanner() {
    "implementation"("com.google.mlkit:barcode-scanning:$mlKitBarcode")
    "implementation"("androidx.camera:camera-camera2:$camera2Version")
    "implementation"("androidx.camera:camera-lifecycle:$camera2Version")
    "implementation"("androidx.camera:camera-view:1.0.0-alpha21")
}

fun DependencyHandlerScope.lifecycle() {
    "implementation"("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    "implementation"("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    "implementation"("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleRuntimeKtx")
}

fun DependencyHandlerScope.navigationComponent() {
    "api"("androidx.navigation:navigation-fragment-ktx:$navVersion")
    "api"("androidx.navigation:navigation-ui-ktx:$navVersion")
}

fun DependencyHandlerScope.coroutines() {
    "api"("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
}

fun DependencyHandlerScope.coroutinesTest() {
    "testImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
}

fun DependencyHandlerScope.scarlet() {
    "api"("$scarletPackage:scarlet:$scarletVersion")
    "api"("$scarletPackage:websocket-okhttp:$scarletVersion")
    "api"("$scarletPackage:stream-adapter-coroutines:$scarletVersion")
    "api"("$scarletPackage:message-adapter-moshi:$scarletVersion")
}

fun DependencyHandlerScope.scarletAndroid() {
    "api"("$scarletPackage:lifecycle-android:$scarletVersion")
}

fun DependencyHandlerScope.scarletTest() {
    "testImplementation"("$scarletPackage:websocket-mockwebserver:$scarletVersion")
    "testImplementation"("$scarletPackage:test-utils:$scarletVersion")
}

fun DependencyHandlerScope.retrofit() {
    "api"("com.squareup.retrofit2:retrofit:$retrofitVersion")
    "api"("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
}

fun DependencyHandlerScope.moshi() {
    "api"("com.squareup.moshi:moshi-adapters:$moshiVersion")
    "api"("com.squareup.moshi:moshi-kotlin:$moshiVersion")
}

fun DependencyHandlerScope.moshiKsp() {
    "ksp"("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")
}

fun DependencyHandlerScope.okhttp() {
    "api"(platform("com.squareup.okhttp3:okhttp-bom:$okhttpVersion"))
    "api"("com.squareup.okhttp3:okhttp")
    "api"("com.squareup.okhttp3:logging-interceptor")
}

fun DependencyHandlerScope.bouncyCastle() {
    "api"("org.bouncycastle:bcprov-jdk15on:$bouncyCastleVersion")
}

fun DependencyHandlerScope.sqlDelightAndroid() {
    "api"("com.squareup.sqldelight:android-driver:$sqlDelightVersion")
    "api"("com.squareup.sqldelight:coroutines-extensions-jvm:$sqlDelightVersion")

}

fun DependencyHandlerScope.sqlCipher() {
    "api"("net.zetetic:android-database-sqlcipher:$sqlCipherVersion")
}

fun DependencyHandlerScope.sqlDelightTest() {
    "testImplementation"("com.squareup.sqldelight:sqlite-driver:$sqlDelightVersion")
    "testImplementation"("org.xerial:sqlite-jdbc:3.8.10.2") {
        // Override the version of sqlite used by sqlite-driver to match Android API 23
        isForce = true
    }
}

fun DependencyHandlerScope.koinJvm() {
    "implementation"("io.insert-koin:koin-core:$koinVersion")
}

fun DependencyHandlerScope.koinAndroid() {
    "api"("io.insert-koin:koin-android:$koinVersion")
}

fun DependencyHandlerScope.koinTest() {
    "testImplementation"("io.insert-koin:koin-test-junit5:$koinVersion")
}

fun DependencyHandlerScope.glide_N_kapt() {
    "implementation"("com.github.bumptech.glide:glide:4.12.0")
    "kapt"("com.github.bumptech.glide:compiler:4.12.0")
}

fun DependencyHandlerScope.multibaseJava() {
    "api"("com.github.multiformats:java-multibase:$multibaseVersion") //https://mvnrepository.com/artifact/com.github.multiformats/java-multibase/1.1.0 vulnerability detected with library
}

fun DependencyHandlerScope.restEasyJava() {
    "implementation"("org.jboss.resteasy:resteasy-jaxrs:$restEasyVersion")
}

fun DependencyHandlerScope.jUnit5() {
    "testImplementation"(platform("org.junit:junit-bom:$jUnit5Version"))
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:$jUnit5Version")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:$jUnit5Version")
    "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")
}

fun DependencyHandlerScope.jUnit5Android() {
    "androidTestImplementation"("org.junit.jupiter:junit-jupiter-api:$jUnit5Version")
    "androidTestImplementation"("de.mannodermaus.junit5:android-test-core:1.3.0")
    "androidTestRuntimeOnly"("de.mannodermaus.junit5:android-test-runner:1.3.0")
}

fun DependencyHandlerScope.androidXTest() {
    "testImplementation"("androidx.test.ext:junit-ktx:1.1.3")
    "testImplementation"("androidx.test:core-ktx:$androidxTestVersion")

    "androidTestImplementation"("androidx.test:core-ktx:$androidxTestVersion")
    "androidTestImplementation"("androidx.test:runner:$androidxTestVersion")
    "androidTestImplementation"("androidx.test:rules:$androidxTestVersion")
}

fun DependencyHandlerScope.robolectric() {
    "testImplementation"("org.robolectric:robolectric:$robolectricVersion")
}

fun DependencyHandlerScope.mockk() {
    "testImplementation"("io.mockk:mockk:$mockkVersion")
}

fun DependencyHandlerScope.testJson() {
    "testImplementation"("org.json:json:$jsonVersion")
}

fun DependencyHandlerScope.timber() {
    "api"("com.jakewharton.timber:timber:$timberVersion")
}

fun DependencyHandlerScope.security() {
    "api"("androidx.security:security-crypto:$androidSecurityVersion")
}


fun DependencyHandlerScope.web3jCrypto() {
    "api"("org.web3j:crypto:$web3jVersion")
}