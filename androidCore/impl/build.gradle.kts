plugins {
    id("com.android.library")
    kotlin("android")
    id("com.squareup.sqldelight")
    id("com.google.devtools.ksp") version kspVersion
    id("publish-module-android")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "android-core-impl"
    extra[KEY_PUBLISH_VERSION] = "1.0.0"
    extra[KEY_SDK_NAME] = "Android Core Impl"
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
    database("AndroidCoreDatabase") {
        packageName = "com.walletconnect.android.impl.core"
        sourceFolders = listOf("core")
    }
}

dependencies {
    debugApi(project(":androidCore:sdk"))
    releaseApi("com.walletconnect:android-core:1.0.0")

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