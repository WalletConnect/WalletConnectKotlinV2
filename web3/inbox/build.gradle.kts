plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp") version kspVersion
    id("publish-module-android")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "web3inbox"
    extra[KEY_PUBLISH_VERSION] = WEB_3_INBOX
    extra[KEY_SDK_NAME] = "web3inbox"
}

android {
    namespace = "com.walletconnect.web3.inbox"
    compileSdk = COMPILE_SDK

    defaultConfig {
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK

        aarMetadata {
            minCompileSdk = MIN_SDK
            targetSdk = TARGET_SDK
        }

        buildConfigField(type = "String", name = "SDK_VERSION", value = "\"${requireNotNull(extra.get(KEY_PUBLISH_VERSION))}\"")
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

dependencies {
    debugImplementation(project(":chat:sdk"))
    debugImplementation(project(":push:sdk"))
    debugImplementation(project(":androidCore:sdk"))

    releaseImplementation("com.walletconnect:chat:$SIGN_VERSION")
    releaseImplementation("com.walletconnect:push:$AUTH_VERSION")
    releaseImplementation("com.walletconnect:android-core:$CORE_VERSION")
}