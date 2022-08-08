plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("com.squareup.sqldelight")
}

android {
    compileSdk = 32

    defaultConfig {
        minSdk = MIN_SDK
        targetSdk = 32

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.time.ExperimentalTime"
    }
}

dependencies {
    okhttp()
    bouncyCastle()
    coroutines()
    moshi()
    scarlet()
    sqlDelight()
    security()
    koin()
    multibaseJava()
}