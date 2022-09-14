import kotlinx.coroutines.flow.flowOf

plugins {
    id("com.android.library")
    kotlin("android")
    id("publish-module")
    id("com.kezong.fat-aar")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "core-android"
    extra[KEY_PUBLISH_VERSION] = "1.0.0"
    extra[KEY_SDK_NAME] = "Android Core"
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
    }
}

fataar {
    transitive = true
}

dependencies {
//    embed(project(":foundation")) {
//        exclude("com.github.WalletConnect.Scarlet")
//        exclude("com.squareup.okhttp3")
//        exclude("io.insert-koin")
////        exclude("com.squareup.moshi")
//        exclude("org.bouncycastle")
//        exclude("com.github.multiformats")
//        exclude("org.jboss.resteasy", "resteasy-jaxrs")
//        exclude("org.junit")
//        exclude("org.junit.jupiter")
//        exclude("org.jetbrains.kotlin")
//        exclude("io.mockk")
//    }
//    implementation(project(":androidCore:common"))
    embed(project(":androidCore:common"))

    koinAndroid()
}