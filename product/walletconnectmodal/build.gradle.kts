plugins {
    id("com.android.library")
    id(libs.plugins.kotlin.android.get().pluginId)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.paparazzi)
    id("publish-module-android")
    id("jacoco-report")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "walletconnect-modal"
    extra[KEY_PUBLISH_VERSION] = WC_MODAL_VERSION
    extra[KEY_SDK_NAME] = "Wallet Connect Modal"
}

android {
    namespace = "com.walletconnect.wcmodal"
    compileSdk = COMPILE_SDK

    defaultConfig {
        minSdk = MIN_SDK

        aarMetadata {
            minCompileSdk = MIN_SDK
        }

        buildConfigField(type = "String", name = "SDK_VERSION", value = "\"${requireNotNull(extra.get(KEY_PUBLISH_VERSION))}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        File("${rootDir.path}/gradle/consumer-rules").listFiles()?.let { proguardFiles ->
            consumerProguardFiles(*proguardFiles)
        }
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
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    tasks.withType(Test::class.java) {
        jvmArgs("-XX:+AllowRedefinitionToAddDeleteMethods")
    }
}

dependencies {
    implementation(libs.bundles.androidxAppCompat)
    implementation(libs.bundles.accompanist)

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

    implementation(libs.coil)
    implementation(libs.bundles.androidxLifecycle)
    api(libs.bundles.androidxNavigation)
    implementation(libs.qrCodeGenerator)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)

    releaseImplementation("com.walletconnect:android-core:$CORE_VERSION")
    releaseImplementation("com.walletconnect:sign:$SIGN_VERSION")
    releaseImplementation("com.walletconnect:modal-core:$MODAL_CORE_VERSION")

    debugImplementation(project(":core:android"))
    debugImplementation(project(":protocol:sign"))
    debugImplementation(project(":core:modal"))
}