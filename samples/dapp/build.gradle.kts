plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    kotlin("android")
    kotlin("kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.appdistribution")
}

android {
    namespace = "com.walletconnect.sample.dapp"
    compileSdk = COMPILE_SDK

    defaultConfig {
        applicationId = "com.walletconnect.sample.dapp"
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
            isDebuggable = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.palette:palette:1.0.0")

    implementation ("io.insert-koin:koin-androidx-compose:3.4.3")
    implementation ("io.coil-kt:coil-compose:2.3.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:31.1.1"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging")

    // Accompanist
    implementation("com.google.accompanist:accompanist-navigation-material:0.27.1")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.27.1")

    debugImplementation(project(":androidCore:sdk"))
    debugImplementation(project(":push:sdk"))
    debugImplementation(project(":web3:modal"))

    releaseImplementation(platform("com.walletconnect:android-bom:$BOM_VERSION"))
    releaseImplementation("com.walletconnect:android-core")
    releaseImplementation("com.walletconnect:push")
    releaseImplementation(project(":web3:modal"))
}
