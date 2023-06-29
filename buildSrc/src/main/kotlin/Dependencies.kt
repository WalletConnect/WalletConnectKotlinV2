import org.gradle.kotlin.dsl.DependencyHandlerScope

fun DependencyHandlerScope.scanner() {
    "implementation"("com.google.mlkit:barcode-scanning:$mlKitBarcode")
    "implementation"("androidx.camera:camera-camera2:$camera2Version")
    "implementation"("androidx.camera:camera-lifecycle:$camera2Version")
    "implementation"("androidx.camera:camera-view:1.0.0-alpha21")
}

fun DependencyHandlerScope.lifecycle() {
    "implementation"("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    "implementation"("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    "implementation"("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
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

fun DependencyHandlerScope.reLinker() {
    "implementation"("com.getkeepsafe.relinker:relinker:$relinkerVersion")
}

fun DependencyHandlerScope.sqlDelightTest() {
    "testImplementation"("com.squareup.sqldelight:sqlite-driver:$sqlDelightVersion")
    "testImplementation"("org.xerial:sqlite-jdbc:3.8.10.2")
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

fun DependencyHandlerScope.wsRestJava() {
    "implementation"("jakarta.ws.rs:jakarta.ws.rs-api:$wsRestJavaVersion")
    "testImplementation"("org.glassfish.jersey.core:jersey-common:3.1.0")
}

// TODO: Replace JUnit5 with JUnit4 since Android tooling does not support JUnit5 yet
fun DependencyHandlerScope.jUnit5() {
    "testImplementation"(platform("org.junit:junit-bom:$jUnit5Version"))
    "testImplementation"("org.junit.jupiter:junit-jupiter-api")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine")
    "testImplementation"("org.junit.jupiter:junit-jupiter-params")

    // JUnit 4 support
    "testImplementation"("junit:junit:4.13.2")
    "testRuntimeOnly"("org.junit.vintage:junit-vintage-engine")

    "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")
}

fun DependencyHandlerScope.jUnit5Android() {
    "testImplementation"(platform("org.junit:junit-bom:$jUnit5Version"))
    "androidTestImplementation"("org.junit.jupiter:junit-jupiter-api")
    "androidTestRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine")
}

fun DependencyHandlerScope.androidXTest() {
    "testImplementation"("androidx.test.ext:junit-ktx:1.1.3")
    "testImplementation"("androidx.test:core-ktx:$androidxTestVersion")

    "androidTestImplementation"("androidx.test:core-ktx:$androidxTestVersion")
    "androidTestImplementation"("androidx.test:runner:1.5.2")
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
    "api"("androidx.security:security-crypto-ktx:$androidSecurityVersion")
}

fun DependencyHandlerScope.web3jCrypto() {
    "api"("org.web3j:crypto:$web3jVersion")
}

fun DependencyHandlerScope.kethereum() {
    "api"("com.github.komputing.kethereum:bip39:$kethereumVersion")
    "api"("com.github.komputing.kethereum:bip39_wordlist_en:$kethereumVersion")
    "api"("com.github.komputing.kethereum:bip32:$kethereumVersion")
    "api"("com.github.komputing.kethereum:model:$kethereumVersion")
    "api"("com.github.komputing.kethereum:crypto_impl_spongycastle:$kethereumVersion")
}

fun DependencyHandlerScope.fcm() {
    "implementation"("com.google.firebase:firebase-messaging:$fcmVersion")
}

fun DependencyHandlerScope.compose() {
    "implementation"(platform("androidx.compose:compose-bom:$composeBomVersion"))
    "implementation"("androidx.compose.ui:ui")
    "implementation"("androidx.compose.ui:ui-tooling-preview")
    "implementation"("androidx.compose.material:material")
    "debugImplementation"("androidx.compose.ui:ui-tooling")
    "debugImplementation"("androidx.compose.ui:ui-test-manifest")
}

fun DependencyHandlerScope.accompanist() {
    "api"("com.google.accompanist:accompanist-navigation-material:$accompanistVersion")
    "implementation"("com.google.accompanist:accompanist-drawablepainter:$accompanistVersion")
    "implementation"("com.google.accompanist:accompanist-navigation-animation:$accompanistVersion")
    "implementation"("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")
    "implementation"("com.google.accompanist:accompanist-pager:$accompanistVersion")
    "implementation"("com.google.accompanist:accompanist-pager-indicators:$accompanistVersion")
}
