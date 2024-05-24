plugins {
    id("com.android.library")
    id(libs.plugins.kotlin.android.get().pluginId)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.google.ksp)
    id("publish-module-android")
//    id("jacoco-report")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "android-core"
    extra[KEY_PUBLISH_VERSION] = CORE_VERSION
    extra[KEY_SDK_NAME] = "Android Core"
}

android {
    namespace = "com.walletconnect.android"
    compileSdk = COMPILE_SDK

    defaultConfig {
        minSdk = MIN_SDK

        buildConfigField(type = "String", name = "SDK_VERSION", value = "\"${requireNotNull(extra.get(KEY_PUBLISH_VERSION))}\"")
        buildConfigField("String", "PROJECT_ID", "\"${System.getenv("WC_CLOUD_PROJECT_ID") ?: ""}\"")
        buildConfigField("Integer", "TEST_TIMEOUT_SECONDS", "${System.getenv("TEST_TIMEOUT_SECONDS") ?: 30}")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments += mutableMapOf("clearPackageData" to "true")

        File("${rootDir.path}/gradle/consumer-rules").listFiles()?.let { proguardFiles ->
            consumerProguardFiles(*proguardFiles)
        }

        ndk.abiFilters += listOf("armeabi-v7a", "x86", "x86_64", "arm64-v8a")
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
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }

    sourceSets {
        getByName("test").resources.srcDirs("src/test/resources")
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"

        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }

        registerManagedDevices()
    }
}

sqldelight {
    databases {
        create("AndroidCoreDatabase") {
            packageName.set("com.walletconnect.android.sdk.core")
            schemaOutputDirectory.set(file("src/main/sqldelight/databases"))
//            generateAsync.set(true) // TODO: Enable once all repository methods have been converted to suspend functions
            verifyMigrations.set(true)
        }
    }
}

dependencies {
    debugApi(project(":foundation"))
    releaseApi("com.walletconnect:foundation:$FOUNDATION_VERSION")

    api(libs.coroutines)
    implementation(libs.scarlet.android)
    implementation(libs.bundles.sqlDelight)
    //noinspection UseTomlInstead
    api("net.zetetic:android-database-sqlcipher:4.5.4@aar")
    implementation(libs.relinker)
    api(libs.androidx.security)
    api(libs.koin.android)
    api(libs.timber)
    ksp(libs.moshi.ksp)
    api(libs.web3jCrypto)
    api(libs.bundles.kethereum)
    api(libs.bundles.retrofit)
    api(libs.beagle.logOkhttp)

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