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
        create("upload") {
            storeFile = File(rootDir, secrets.getProperty("WC_FILENAME_UPLOAD"))
            storePassword = secrets.getProperty("WC_STORE_PASSWORD_UPLOAD")
            keyAlias = secrets.getProperty("WC_KEYSTORE_ALIAS")
            keyPassword = secrets.getProperty("WC_KEY_PASSWORD_UPLOAD")
        }

        create("internal_release") {
            storeFile = File(rootDir, secrets.getProperty("WC_FILENAME_INTERNAL"))
            storePassword = secrets.getProperty("WC_STORE_PASSWORD_INTERNAL")
            keyAlias = secrets.getProperty("WC_KEYSTORE_ALIAS")
            keyPassword = secrets.getProperty("WC_KEY_PASSWORD_INTERNAL")
        }

        getByName("debug") {
            storeFile = File(rootDir, secrets.getProperty("WC_FILENAME_DEBUG"))
            storePassword = secrets.getProperty("WC_STORE_PASSWORD_DEBUG")
            keyAlias = secrets.getProperty("WC_KEYSTORE_ALIAS_DEBUG")
            keyPassword = secrets.getProperty("WC_KEY_PASSWORD_DEBUG")
        }
    }

    buildTypes {
        // Google Play Internal Track
        getByName("release") {
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("upload")
            versionNameSuffix = System.getenv("GITHUB_RUN_NUMBER")?.let { ".$it" } ?: ""
            defaultConfig.versionCode = "$SAMPLE_VERSION_CODE${System.getenv("GITHUB_RUN_NUMBER") ?: ""}".toInt()
            firebaseAppDistribution {
                artifactType = "AAB"
                serviceCredentialsFile = File(rootDir, "credentials.json").path
//                groups = "design-team, javascript-team, kotlin-team, rust-team, swift-team, wc-testers"
            }
        }

        // Firebase App Distribution
        create("internal") {
            isMinifyEnabled = true
            isDebuggable = true
            applicationIdSuffix(".internal")
            matchingFallbacks += listOf("debug")
            signingConfig = signingConfigs.getByName("internal_release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            versionNameSuffix = "${System.getenv("GITHUB_RUN_NUMBER")?.let { ".$it" } ?: ""}-internal"
            defaultConfig.versionCode = "$SAMPLE_VERSION_CODE${System.getenv("GITHUB_RUN_NUMBER") ?: ""}".toInt()
            firebaseAppDistribution {
                artifactType = "APK"
                serviceCredentialsFile = File(rootDir, "credentials.json").path
//                groups = "design-team, javascript-team, kotlin-team, rust-team, swift-team, wc-testers"
            }
        }

        getByName("debug") {
            applicationIdSuffix(".debug")
            signingConfig = signingConfigs.getByName("debug")
            versionNameSuffix = "${System.getenv("GITHUB_RUN_NUMBER")?.let { ".$it" } ?: ""}-debug"
            defaultConfig.versionCode = "$SAMPLE_VERSION_CODE${System.getenv("GITHUB_RUN_NUMBER") ?: ""}".toInt()
            firebaseAppDistribution {
                artifactType = "APK"
                serviceCredentialsFile = File(rootDir, "credentials.json").path
//                groups = "design-team, javascript-team, kotlin-team, rust-team, swift-team, wc-testers"
            }
        }
    }
}