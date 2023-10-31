plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.sqlDelight)
    id("com.google.devtools.ksp") version kspVersion
    id("publish-module-android")
    id("jacoco-report")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "chat"
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

    retrofit()
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