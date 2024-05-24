plugins {
    id("com.android.library")
    id(libs.plugins.kotlin.android.get().pluginId)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.google.ksp)
    id("publish-module-android")
//    id("jacoco-report")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "auth"
    extra[KEY_PUBLISH_VERSION] = AUTH_VERSION
    extra[KEY_SDK_NAME] = "Auth"
}

android {
    namespace = "com.walletconnect.auth"
    compileSdk = COMPILE_SDK

    defaultConfig {
        minSdk = MIN_SDK

        buildConfigField(
            type = "String",
            name = "SDK_VERSION",
            value = "\"${requireNotNull(extra.get(KEY_PUBLISH_VERSION))}\""
        )
        buildConfigField(
            "String",
            "PROJECT_ID",
            "\"${System.getenv("WC_CLOUD_PROJECT_ID") ?: ""}\""
        )
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "${rootDir.path}/gradle/proguard-rules/sdk-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = jvmVersion
        targetCompatibility = jvmVersion
    }

    kotlinOptions {
        jvmTarget = jvmVersion.toString()
    }

    buildFeatures {
        buildConfig = true
    }
}

sqldelight {
    databases {
        create("AuthDatabase") {
            packageName.set("com.walletconnect.auth")
            schemaOutputDirectory.set(file("src/main/sqldelight/databases"))
            generateAsync.set(true)
            verifyMigrations.set(true)
            verifyDefinitions.set(true)
        }
    }
}


dependencies {
    debugImplementation(project(":core:android"))
    releaseImplementation("com.walletconnect:android-core:$CORE_VERSION")

    ksp(libs.moshi.ksp)
    implementation(libs.bundles.sqlDelight)
    api(libs.web3jCrypto)

    testImplementation(libs.bundles.androidxTest)
    testImplementation(libs.robolectric)
    testImplementation(libs.json)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.bundles.scarlet.test)
    testImplementation(libs.bundles.sqlDelight.test)

    androidTestUtil(libs.androidx.testOrchestrator)
    androidTestImplementation(libs.bundles.androidxAndroidTest)
}