plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp") version kspVersion
    id("publish-module-android")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "walletconnect-modal"
    extra[KEY_PUBLISH_VERSION] = WC_MODAL_VERSION
    extra[KEY_SDK_NAME] = "Wallet Connect Modal"
}

android {
    namespace = "com.walletconnect.modal"
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

    appCompat()
    accompanist()
    compose()
    coil()
    lifecycle()
    navigationComponent()
    qrCodeGenerator()
    //override compose material to fix crash at modalsheet
    implementation("androidx.compose.material:material:1.5.0-alpha04")

    jUnit5()

    releaseImplementation(platform("com.walletconnect:android-bom:$BOM_VERSION"))
    releaseImplementation("com.walletconnect:android-core")
    releaseImplementation("com.walletconnect:sign")
    releaseImplementation("com.walletconnect:auth")
    releaseImplementation(project(":core:modalCore"))

    debugImplementation(project(":androidCore:sdk"))
    debugImplementation(project(":sign:sdk"))
    debugImplementation(project(":auth:sdk"))
    debugImplementation(project(":core:modalCore"))
}