import com.android.build.gradle.BaseExtension
import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import java.util.Properties

plugins {
    id("com.google.firebase.appdistribution")
}

private val Project.secrets: Properties
    get() = rootProject.file("secrets.properties").let { secretsFile ->
        Properties().apply {
            load(secretsFile.inputStream())
        }
    }

project.extensions.configure(BaseExtension::class.java) {
    signingConfigs {
        create("internal_release") {
            storeFile = File(rootDir, secrets.getProperty("WC_FILENAME_INTERNAL"))
            storePassword = secrets.getProperty("WC_STORE_PASSWORD_INTERNAL")
            keyAlias = secrets.getProperty("WC_KEYSTORE_ALIAS")
            keyPassword = secrets.getProperty("WC_KEY_PASSWORD_INTERNAL")
        }
        create("upload") {
            storeFile = File(rootDir, secrets.getProperty("WC_FILENAME_UPLOAD"))
            storePassword = secrets.getProperty("WC_STORE_PASSWORD_UPLOAD")
            keyAlias = secrets.getProperty("WC_KEYSTORE_ALIAS")
            keyPassword = secrets.getProperty("WC_KEY_PASSWORD_UPLOAD")
        }
    }

    buildTypes {
        // Google Play Internal Track
        getByName("release") {
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("upload")
            firebaseAppDistribution {
                artifactType = "AAB"
                groups = "design-team, javascript-team, kotlin-team, rust-team, swift-team, wc-testers"
            }
        }

        // Firebase App Distribution
        create("internal") {
            initWith(getByName("release"))
            isDebuggable = true
            applicationIdSuffix(".internal")
            matchingFallbacks += "release"
            signingConfig = signingConfigs.getByName("internal_release")
            firebaseAppDistribution {
                artifactType = "APK"
                groups = "design-team, javascript-team, kotlin-team, rust-team, swift-team, wc-testers"
            }
        }

        getByName("debug") {
            applicationIdSuffix(".debug")
            signingConfig = signingConfigs.getByName("debug")
            firebaseAppDistribution {
                artifactType = "APK"
                groups = "design-team, javascript-team, kotlin-team, rust-team, swift-team, wc-testers"
            }
        }
    }
}