plugins {
    id("com.android.library")
    kotlin("android")
    id("com.squareup.sqldelight")
    id("com.google.devtools.ksp") version kspVersion
}

android {
    compileSdk = 32

    defaultConfig {
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation(project(":android_core_impl"))

    retrofit()
    navigationComponent()
    moshiKsp()

    androidXTest()
    jUnit5()
    jUnit5Android()
    robolectric()
    mockk()
    testJson()
    coroutinesTest()
    scarletTest()
    sqlDelightTest()
}