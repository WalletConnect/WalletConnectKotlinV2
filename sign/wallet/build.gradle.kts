plugins {
    id("com.android.application")
//    id("com.google.gms.google-services")  // This sample is planned to be removed
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdk = COMPILE_SDK

    defaultConfig {
        applicationId = "com.walletconnect.wallet"
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isDebuggable = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")
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
        viewBinding = true
    }
}

dependencies {
    implementation(project(":sign:samples_common"))

    debugImplementation(project(":androidCore:sdk"))
    debugImplementation(project(":sign:sdk"))
    debugImplementation(project(":push:sdk"))

    releaseImplementation(platform("com.walletconnect:android-bom:$BOM_VERSION"))
    releaseImplementation("com.walletconnect:android-core")
    releaseImplementation("com.walletconnect:sign")
    releaseImplementation(project(":push:sdk"))

    implementation(platform("com.google.firebase:firebase-bom:31.0.0"))
    implementation("com.google.firebase:firebase-messaging")

    scanner()
    glide_N_kapt()
    implementation("androidx.fragment:fragment-ktx:1.5.5")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}