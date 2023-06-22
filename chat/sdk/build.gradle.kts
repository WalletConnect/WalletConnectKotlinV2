plugins {
    id("com.android.library")
    kotlin("android")
    id("com.squareup.sqldelight")
    id("com.google.devtools.ksp") version kspVersion
    id("publish-module-android")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "chat"
    extra[KEY_PUBLISH_VERSION] = CHAT_VERSION
    extra[KEY_SDK_NAME] = "Chat"
}

android {
    namespace = "com.walletconnect.chat"
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
    }

    testOptions.unitTests {
        isIncludeAndroidResources = true
        isReturnDefaultValues = true
    }
}

sqldelight {
    database("ChatDatabase") {
        packageName = "com.walletconnect.chat"
        schemaOutputDirectory = file("src/debug/sqldelight/databases")
        verifyMigrations = true
    }
}

dependencies {
    debugImplementation(project(":androidCore:sdk"))
    releaseImplementation("com.walletconnect:android-core:$CORE_VERSION")

    retrofit()
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