plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("com.squareup.sqldelight")
    id("com.google.devtools.ksp") version kspVersion
    id("publish-module-android")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "auth"
    extra[KEY_PUBLISH_VERSION] = "1.0.0"
    extra[KEY_SDK_NAME] = "Auth"
}

android {
    compileSdk = COMPILE_SDK

    defaultConfig {
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK

        buildConfigField(type = "String", name= "sdkVersion", value = "\"1.0.0-alpha01\"")
        buildConfigField("String", "PROJECT_ID", "\"${System.getenv("WC_CLOUD_PROJECT_ID") ?: ""}\"")
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

sqldelight {
    database("AuthDatabase") {
        packageName = "com.walletconnect.auth"
        schemaOutputDirectory = file("src/main/sqldelight/databases")
        verifyMigrations = true
    }
}

dependencies {
    debugImplementation(project(":androidCore:impl"))
    releaseImplementation("com.walletconnect:android-core-impl:1.1.0")

    okhttp()
    timber()
    moshiKsp()
    androidXTest()
    robolectric()
    mockk()
    testJson()
    coroutinesTest()
    scarletTest()
    sqlDelightTest()
    jUnit5()
    jUnit5Android()
    web3jCrypto()
}