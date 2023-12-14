plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("signing-config")
//    id("io.sentry.android.gradle") version "3.12.0"
}

android {
    namespace = "com.walletconnect.sample.wallet"
    compileSdk = COMPILE_SDK
    // hash of all sdk versions from Versions.kt

    defaultConfig {
        applicationId = "com.walletconnect.sample.wallet"
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK
        versionName = SAMPLE_VERSION_NAME
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "PROJECT_ID","\"${System.getenv("WC_CLOUD_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "BOM_VERSION", "\"${BOM_VERSION}\"")
    }

    compileOptions {
        sourceCompatibility = jvmVersion
        targetCompatibility = jvmVersion
    }

    kotlinOptions {
        jvmTarget = jvmVersion.toString()
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = composeCompilerVersion
    }
}

dependencies {
    implementation(project(":sample:common"))
    implementation("androidx.compose.material3:material3:1.0.0-alpha08")

    firebaseMessaging()
    firebaseChrashlytics()
    appCompat()

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")

    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.palette:palette:1.0.0")

    // Glide
    implementation("com.github.skydoves:landscapist-glide:2.1.0")

    // Accompanist
    accompanist()

    // Compose
    compose()
    coil()

    implementation("com.google.android.gms:play-services-mlkit-barcode-scanning:18.1.0")
    implementation("androidx.lifecycle:lifecycle-process:2.5.1")
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    // CameraX
    implementation("androidx.camera:camera-camera2:1.2.0")
    implementation("androidx.camera:camera-lifecycle:1.2.0")
    implementation("androidx.camera:camera-view:1.0.0-alpha31")

    // Zxing
    implementation("com.google.zxing:core:3.5.0")

    // MixPanel
    implementation("com.mixpanel.android:mixpanel-android:7.3.1")

    // Unit Tests
    jUnit4()
    mockk()

    // WalletConnect
    debugImplementation(project(":core:android"))
    debugImplementation(project(":product:web3wallet"))
    debugImplementation(project(":protocol:notify"))

    internalImplementation(project(":core:android"))
    internalImplementation(project(":product:web3wallet"))
    internalImplementation(project(":protocol:notify"))

    releaseImplementation(platform("com.walletconnect:android-bom:$BOM_VERSION"))
    releaseImplementation("com.walletconnect:android-core")
    releaseImplementation("com.walletconnect:web3wallet")
    releaseImplementation("com.walletconnect:notify")
}
