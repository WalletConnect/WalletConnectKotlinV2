plugins {
    id("com.android.library")
    kotlin("android")
    id("com.squareup.sqldelight")
    id("com.google.devtools.ksp") version kspVersion
    id("publish-module-android")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "android-core-impl"
    extra[KEY_PUBLISH_VERSION] = CORE_VERSION
    extra[KEY_SDK_NAME] = "Android Core Impl"
}

android {
    compileSdk = COMPILE_SDK

    defaultConfig {
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "${rootDir.path}/gradle/proguard-rules/sdk-rules.pro")
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
        schemaOutputDirectory = file("src/main/sqldelight/databases")
        verifyMigrations = true
    }
}

dependencies {
    debugApi(project(":androidCore:sdk"))
    releaseApi("com.walletconnect:android-core:$CORE_VERSION")

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
    coroutinesTest()
}