plugins {
    id(libs.plugins.android.application.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    id(libs.plugins.kotlin.kapt.get().pluginId)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    id("signing-config")
}

android {
    namespace = "com.walletconnect.sample.modal"
    compileSdk = COMPILE_SDK

    defaultConfig {
        applicationId = "com.walletconnect.sample.modal"
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK
        versionName = SAMPLE_VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "PROJECT_ID", "\"${System.getenv("WC_CLOUD_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "BOM_VERSION", "\"${BOM_VERSION}\"")
    }

    lint {
        abortOnError = true
        ignoreWarnings = true
        warningsAsErrors = false
    }

    compileOptions {
        sourceCompatibility = jvmVersion
        targetCompatibility = jvmVersion
    }
    kotlinOptions {
        jvmTarget = jvmVersion.toString()
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    implementation(project(":sample:common"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.compose.lifecycle)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.compose.ui.test.junit)
    androidTestImplementation(libs.androidx.compose.navigation.testing)

    implementation(libs.bundles.accompanist)
    implementation(libs.bundles.androidxAppCompat)
    implementation(libs.bundles.androidxLifecycle)
    api(libs.bundles.androidxNavigation)

    debugImplementation(project(":core:android"))
    debugImplementation(project(":product:web3modal"))

    internalImplementation(project(":core:android"))
    internalImplementation(project(":product:web3modal"))

    releaseImplementation(platform("com.walletconnect:android-bom:$BOM_VERSION"))
    releaseImplementation("com.walletconnect:android-core")
    releaseImplementation("com.walletconnect:web3modal")
}