plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    kotlin("android")
    kotlin("kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("signing-config")
}

android {
    namespace = "com.walletconnect.sample.dapp"
    compileSdk = COMPILE_SDK

    defaultConfig {
        applicationId = "com.walletconnect.sample.dapp"
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK
        versionName = SAMPLE_VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "PROJECT_ID", "\"${System.getenv("WC_CLOUD_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "BOM_VERSION", "\"${BOM_VERSION ?: ""}\"")
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

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.palette:palette:1.0.0")

    implementation("io.insert-koin:koin-androidx-compose:3.4.3")
    implementation("io.coil-kt:coil-compose:2.3.0")
    implementation("androidmads.library.qrgenearator:QRGenearator:1.0.4")

    compose()
    accompanist()

    firebaseMessaging()
    firebaseChrashlytics()

    debugImplementation(project(":core:android"))
    debugImplementation(project(":product:walletconnectmodal"))

    internalImplementation(project(":core:android"))
    internalImplementation(project(":product:walletconnectmodal"))

    releaseImplementation(platform("com.walletconnect:android-bom:$BOM_VERSION"))
    releaseImplementation("com.walletconnect:android-core")
    releaseImplementation(project(":product:walletconnectmodal"))
}
