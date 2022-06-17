plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("com.squareup.sqldelight")
    `maven-publish`
}

tasks.withType<Test> {
    useJUnitPlatform()
}

android {
    compileSdk = 32

    defaultConfig {
        minSdk = 23
        targetSdk = 32

        testInstrumentationRunner = "com.walletconnect.sign.WCTestRunner"
        testInstrumentationRunnerArguments += mutableMapOf("runnerBuilder" to "de.mannodermaus.junit5.AndroidJUnit5Builder")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

    testOptions.unitTests.isIncludeAndroidResources = true

    packagingOptions {
        resources.excludes += setOf(
            "META-INF/LICENSE.md",
            "META-INF/LICENSE-notice.md",
            "META-INF/AL2.0",
            "META-INF/LGPL2.1"
        )
    }
}

dependencies {
    okhttp()
    bouncyCastle()
    coroutines()
    moshi()
    scarlet()
    sqlDelight()
    security()
    koin()

    androidXTest()
    jUnit5()
    robolectric()
    mockk()
    timber()
}