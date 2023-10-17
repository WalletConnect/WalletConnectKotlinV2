plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.sqlDelight)
    id("com.google.devtools.ksp") version kspVersion
    id("publish-module-android")
    id("jacoco-report")
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

        aarMetadata {
            minCompileSdk = MIN_SDK
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

        registerManagedDevices()
    }

    buildFeatures {
        buildConfig = true
    }
}

sqldelight {
    databases {
        create("SignDatabase") {
            packageName.set("com.walletconnect.sign")
            schemaOutputDirectory.set(file("src/debug/sqldelight/databases"))
            verifyMigrations.set(true)
            verifyDefinitions.set(true)
        }
    }
}

dependencies {
    debugImplementation(project(":core:android"))
    releaseImplementation("com.walletconnect:android-core:$CORE_VERSION")

    moshiKsp()
    implementation(libs.bundles.sqlDelight)

    androidXTest()
    jUnit4()
    robolectric()
    mockk()
    testJson()
    coroutinesTest()
    scarletTest()
    testImplementation(libs.bundles.sqlDelightTest)
}