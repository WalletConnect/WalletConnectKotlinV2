plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.appdistribution")
}

android {
    namespace = "com.walletconnect.sample.web3inbox"
    compileSdk = COMPILE_SDK

    defaultConfig {
        applicationId = "com.walletconnect.sample.web3inbox"
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK
        versionCode = SAMPLE_VERSION_CODE
        versionName = SAMPLE_VERSION_NAME + ".DOG"

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

    firebaseMessaging()
    firebaseChrashlytics()

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

    // WalletConnect
    debugImplementation(project(":core:android"))
    debugImplementation(project(":product:web3wallet"))
    debugImplementation(project(":product:web3inbox"))
    debugImplementation(project(":protocol:notify"))
    debugImplementation(project(":product:walletconnectmodal"))

    releaseImplementation(platform("com.walletconnect:android-bom:$BOM_VERSION"))
    releaseImplementation("com.walletconnect:android-core")
    releaseImplementation("com.walletconnect:web3wallet")
    releaseImplementation("com.walletconnect:web3inbox")
    releaseImplementation("com.walletconnect:notify")
    releaseImplementation("com.walletconnect:walletconnect-modal")
}