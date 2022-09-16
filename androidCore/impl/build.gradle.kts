plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp") version kspVersion
    id("com.squareup.sqldelight")
}

android {
    compileSdk = COMPILE_SDK

    defaultConfig {
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK

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

sqldelight {
    database("Database") {
        packageName = "com.walletconnect.android.impl"
    }
}

dependencies {
    debugApi(project(":androidCore:sdk"))
    releaseApi("com.walletconnect:android-core:1.0.0")

    debugApi(project(":androidCore:common"))
    releaseApi("com.walletconnect:android-core-common:1.0.0")

    bouncyCastle()
    coroutines()
    moshiKsp()
    moshi()
    scarlet()
    scarletAndroid()
    sqlDelightAndroid()
    sqlCipher()
    security()
    koinAndroid()
    multibaseJava()
    timber()

    jUnit5()
    jUnit5Android()
    mockk()
}