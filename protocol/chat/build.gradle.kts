plugins {
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.google.ksp)
    id("publish-module-android")
    id("jacoco-report")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = CHAT
    extra[KEY_PUBLISH_VERSION] = CHAT_VERSION
    extra[KEY_SDK_NAME] = "Chat"
}

android {
    namespace = "com.walletconnect.chat"
    compileSdk = COMPILE_SDK

    defaultConfig {
        minSdk = MIN_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "${rootDir.path}/gradle/proguard-rules/sdk-rules.pro")
        }
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
    }

    testOptions.unitTests {
        isIncludeAndroidResources = true
        isReturnDefaultValues = true
    }
}

sqldelight {
    databases {
        create("ChatDatabase") {
            packageName.set("com.walletconnect.chat")
            schemaOutputDirectory.set(file("src/main/sqldelight/databases"))
//            generateAsync.set(true) // TODO: Enable once all repository methods have been converted to suspend functions
            verifyMigrations.set(true)
            verifyDefinitions.set(true)
        }
    }
}

dependencies {
    debugImplementation(project(":core:android"))
    releaseImplementation("com.walletconnect:android-core:$CORE_VERSION")

    implementation(libs.bundles.retrofit)
    ksp(libs.moshi.ksp)
    implementation(libs.bundles.sqlDelight)

    testImplementation(libs.bundles.androidxTest)
    testImplementation(libs.robolectric)
    testImplementation(libs.json)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.bundles.scarlet.test)
    testImplementation(libs.bundles.sqlDelight.test)
}