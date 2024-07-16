plugins {
    id("com.android.library")
    id(libs.plugins.kotlin.android.get().pluginId)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.google.ksp)
    id("publish-module-android")
    id("jacoco-report")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "notify"
    extra[KEY_PUBLISH_VERSION] = NOTIFY_VERSION
    extra[KEY_SDK_NAME] = "Notify"
}

android {
    namespace = "com.walletconnect.notify"
    compileSdk = COMPILE_SDK

    defaultConfig {
        minSdk = MIN_SDK

        aarMetadata {
            minCompileSdk = MIN_SDK
        }

        buildConfigField(type = "String", name = "SDK_VERSION", value = "\"${requireNotNull(extra.get(KEY_PUBLISH_VERSION))}\"")
        buildConfigField("String", "PROJECT_ID", "\"${System.getenv("WC_CLOUD_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "NOTIFY_INTEGRATION_TESTS_PROJECT_ID", "\"${System.getenv("NOTIFY_INTEGRATION_TESTS_PROJECT_ID") ?: ""}\"")
        buildConfigField("String", "NOTIFY_INTEGRATION_TESTS_SECRET", "\"${System.getenv("NOTIFY_INTEGRATION_TESTS_SECRET") ?: ""}\"")
        buildConfigField("Integer", "TEST_TIMEOUT_SECONDS", "${System.getenv("TEST_TIMEOUT_SECONDS") ?: 60}")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments += mutableMapOf("clearPackageData" to "true")
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
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.time.ExperimentalTime"
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"

        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }

        registerManagedDevices()
    }

    buildFeatures {
        buildConfig = true
    }
}

sqldelight {
    databases {
        create("NotifyDatabase") {
            packageName.set("com.walletconnect.notify")
            schemaOutputDirectory.set(file("src/main/sqldelight/databases"))
//            generateAsync.set(true) TODO uncomment once all SDKs have this flag enabled
            verifyMigrations.set(true)
            verifyDefinitions.set(true)
        }
    }
}

dependencies {
    debugImplementation(project(":core:android"))
    releaseImplementation("com.walletconnect:android-core:$CORE_VERSION")

    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    ksp(libs.moshi.ksp)
    implementation(libs.bundles.sqlDelight)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    testImplementation(libs.bundles.androidxTest)
    testImplementation(libs.robolectric)
    testImplementation(libs.json)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.bundles.scarlet.test)
    testImplementation(libs.bundles.sqlDelight.test)

    androidTestUtil(libs.androidx.testOrchestrator)
    androidTestImplementation(libs.bundles.androidxAndroidTest)
}