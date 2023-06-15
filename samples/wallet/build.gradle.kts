plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.appdistribution")
}

android {
    namespace = "com.walletconnect.sample.wallet"
    compileSdk = COMPILE_SDK
    // hash of all sdk versions from Versions.kt

    defaultConfig {
        applicationId = "com.walletconnect.sample.wallet"
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK
        versionCode = SAMPLE_VERSION_CODE
        versionName = SAMPLE_VERSION_NAME
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "PROJECT_ID", "\"${System.getenv("WC_CLOUD_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "BOM_VERSION", "\"${BOM_VERSION ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isDebuggable = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
            firebaseAppDistribution {
                artifactType = "APK"
                groups = "javascript-team, kotlin-team, rust-team, swift-team, wc-testers"
            }
        }
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeCompilerVersion
    }
}

dependencies {
    implementation(project(":sign:samples_common"))

    implementation(platform("com.google.firebase:firebase-bom:31.1.1"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")

    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.palette:palette:1.0.0")

    implementation(platform("com.google.firebase:firebase-bom:31.0.0"))
    implementation("com.google.firebase:firebase-messaging")

    // Glide
    implementation("com.github.skydoves:landscapist-glide:2.1.0")

    // Accompanist
    accompanist()

    // Compose
    compose()

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

    // Unit Tests
    jUnit5()
    mockk()

    // WalletConnect
    debugImplementation(project(":androidCore:sdk"))
    debugImplementation(project(":web3:wallet"))
    debugImplementation(project(":web3:inbox"))
    debugImplementation(project(":push:sdk"))

    releaseImplementation(project(":androidCore:sdk"))
    releaseImplementation(project(":web3:wallet"))
    releaseImplementation(project(":web3:inbox"))
    releaseImplementation(project(":push:sdk"))
//    releaseImplementation(platform("com.walletconnect:android-bom:$BOM_VERSION"))
//    releaseImplementation("com.walletconnect:android-core")
//    releaseImplementation("com.walletconnect:web3wallet")
//    releaseImplementation("com.walletconnect:web3inbox")
//    releaseImplementation("com.walletconnect:push")
}