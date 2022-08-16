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

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    api(project(":foundation"))

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