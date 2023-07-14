plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp") version kspVersion
    id("publish-module-android")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "web3modal"
    extra[KEY_PUBLISH_VERSION] = WEB_3_MODAL
    extra[KEY_SDK_NAME] = "web3modal"
}

android {
    namespace = "com.walletconnect.web3.modal"
    compileSdk = COMPILE_SDK

    defaultConfig {
        minSdk = MIN_SDK

        aarMetadata {
            minCompileSdk = MIN_SDK
        }

        buildConfigField(type = "String", name = "SDK_VERSION", value = "\"${requireNotNull(extra.get(KEY_PUBLISH_VERSION))}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeCompilerVersion
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("com.google.android.material:material:1.8.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")

    //compose
    compose()
    //override compose material to fix crash at modalsheet
    implementation("androidx.compose.material:material:1.5.0-alpha04")

    //coil
    implementation("io.coil-kt:coil-compose:2.3.0")

    // accompanist
    accompanist()

    // Qrcode generator
    implementation("com.github.alexzhirkevich:custom-qr-generator:1.6.1")

    releaseImplementation(platform("com.walletconnect:android-bom:$BOM_VERSION"))
    releaseImplementation("com.walletconnect:android-core")
    releaseImplementation("com.walletconnect:sign")
    releaseImplementation("com.walletconnect:auth")
    releaseImplementation(project(":core:modalCore"))

    debugImplementation(project(":core:android"))
    debugImplementation(project(":protocol:sign"))
    debugImplementation(project(":protocol:auth"))
    debugImplementation(project(":core:modalCore"))

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}