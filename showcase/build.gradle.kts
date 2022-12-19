plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.relay") version "0.3.01"
}

android {
    namespace = "com.walletconnect.showcase"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.walletconnect.showcase"
        minSdk = MIN_SDK
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "PROJECT_ID", "\"${System.getenv("WC_CLOUD_PROJECT_ID") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.1"
    }
}

dependencies {
    implementation(project(":sign:samples_common"))

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.3.1")

    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.palette:palette:1.0.0")

    // Glide
    implementation("com.github.skydoves:landscapist-glide:2.1.0")

    // Accompanist
    implementation("com.google.accompanist:accompanist-navigation-material:0.27.1")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.27.1")
    implementation("com.google.accompanist:accompanist-pager:0.27.1")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.27.1")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2022.11.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material")
    implementation("com.google.android.gms:play-services-mlkit-barcode-scanning:18.1.0")
    implementation("androidx.lifecycle:lifecycle-process:2.5.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    // CameraX
    implementation("androidx.camera:camera-camera2:1.0.2")
    implementation("androidx.camera:camera-lifecycle:1.0.2")
    implementation("androidx.camera:camera-view:1.0.0-alpha31")

    // Zxing
    implementation("com.google.zxing:core:3.3.3")

    // WalletConnect
    implementation(platform("com.walletconnect:android-bom:1.1.0")) // Commented as bom is currently missing auth
    implementation("com.walletconnect:android-core")
    implementation("com.walletconnect:web3wallet")
}