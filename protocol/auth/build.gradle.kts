plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.sqlDelight)
    id("com.google.devtools.ksp") version kspVersion
    id("publish-module-android")
    id("jacoco-report")
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

    okhttp()
    moshiKsp()
    implementation(libs.bundles.sqlDelight)

    androidXTest()
    robolectric()
    mockk()
    testJson()
    coroutinesTest()
    scarletTest()
    testImplementation(libs.bundles.sqlDelightTest)
    jUnit4()
    web3jCrypto()
}