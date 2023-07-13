plugins {
    id("com.android.library")
    kotlin("android")
    id("com.squareup.sqldelight")
    id("com.google.devtools.ksp") version kspVersion
    id("publish-module-android")
//    id("de.mannodermaus.android-junit5") version "1.9.3.0"
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "sign"
    extra[KEY_PUBLISH_VERSION] = SIGN_VERSION
    extra[KEY_SDK_NAME] = "Sign"
}

android {
    namespace = "com.walletconnect.sign"
    compileSdk = COMPILE_SDK

    defaultConfig {
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK

        aarMetadata {
            minCompileSdk = MIN_SDK
            targetSdk = TARGET_SDK
        }

        buildConfigField(type = "String", name = "SDK_VERSION", value = "\"${requireNotNull(extra.get(KEY_PUBLISH_VERSION))}\"")
        buildConfigField("String", "PROJECT_ID", "\"${System.getenv("WC_CLOUD_PROJECT_ID") ?: ""}\"")
        buildConfigField("Integer", "TEST_TIMEOUT_SECONDS", "${System.getenv("TEST_TIMEOUT_SECONDS") ?: 10}")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments += mutableMapOf("clearPackageData" to "true")
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

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"

        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

sqldelight {
    database("SignDatabase") {
        packageName = "com.walletconnect.sign"
        schemaOutputDirectory = file("src/debug/sqldelight/databases")
        verifyMigrations = true
    }
}

dependencies {
    debugImplementation(project(":core:android"))
    releaseImplementation("com.walletconnect:android-core:$CORE_VERSION")

    androidTestUtil("androidx.test:orchestrator:1.4.2")
    moshiKsp()
    androidXTest()
    jUnit4()
    robolectric()
    mockk()
    testJson()
    coroutinesTest()
    scarletTest()
    sqlDelightTest()
}