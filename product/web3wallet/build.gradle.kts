plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp") version kspVersion
    id("publish-module-android")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "web3wallet"
    extra[KEY_PUBLISH_VERSION] = WEB_3_WALLET
    extra[KEY_SDK_NAME] = "web3wallet"
}

android {
    namespace = "com.walletconnect.web3.wallet"
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

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    debugImplementation(project(":core:android"))
    debugImplementation(project(":protocol:sign"))
    debugImplementation(project(":protocol:auth"))

    releaseImplementation("com.walletconnect:android-core:$CORE_VERSION")
    releaseImplementation("com.walletconnect:sign:$SIGN_VERSION")
    releaseImplementation("com.walletconnect:auth:$AUTH_VERSION")
}