import org.apache.commons.io.output.ByteArrayOutputStream

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
    namespace = "com.walletconnect.sign"
    compileSdk = 32

    defaultConfig {
        minSdk = MIN_SDK
        targetSdk = 32

        aarMetadata {
            minCompileSdk = MIN_SDK
            targetSdk = 32
        }

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

    testOptions.unitTests {
        isIncludeAndroidResources = true
        isReturnDefaultValues = true
    }

    packagingOptions {
        resources.excludes += setOf(
            "META-INF/LICENSE.md",
            "META-INF/LICENSE-notice.md",
            "META-INF/AL2.0",
            "META-INF/LGPL2.1"
        )
    }
}

fun getVersionName(): String {
    return try {
        val code = ByteArrayOutputStream()

        exec {
            commandLine("git", "tag", "--list")
            standardOutput = code
        }

        code.toString().split("\n").run {
            get(this.size - 3)
        }
    } catch(e: Exception) {
        "default"
    }
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = "com.walletconnect"
                artifactId = "sign"
                version = getVersionName()

                afterEvaluate {
                    from(components["release"])
                }
            }
        }
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
    multibaseJava()

    androidXTest()
    jUnit5()
    robolectric()
    mockk()
    timber()
}