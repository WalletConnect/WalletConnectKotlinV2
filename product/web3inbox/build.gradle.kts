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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeCompilerVersion
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2022.11.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material")
    implementation("androidx.activity:activity-compose:1.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation(platform("com.google.firebase:firebase-bom:31.0.0"))
    implementation("com.google.firebase:firebase-messaging")

    moshiKsp()

    // WalletConnect
    debugImplementation(project(":core:android"))
    debugImplementation(project(":protocol:chat"))
    debugImplementation(project(":protocol:push"))

    releaseImplementation("com.walletconnect:android-core:$CORE_VERSION")
    releaseImplementation("com.walletconnect:chat:$CHAT_VERSION")
    releaseImplementation("com.walletconnect:push:$PUSH_VERSION")
}