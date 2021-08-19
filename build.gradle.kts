import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("android") version kotlinVersion apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter() // Warning: this repository is going to shut down soon
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.android")

    configure<BaseExtension> {
        compileSdkVersion(30)

        defaultConfig {
            minSdkVersion(23)
            targetSdkVersion(30)
            versionCode(1)
            versionName("1.0")

            testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")
            consumerProguardFiles("consumer-rules.pro")
        }

        sourceSets {
            map { it.java.srcDirs("src/${it.name}/kotlin") }
        }

        buildTypes {
            getByName("release") {
                minifyEnabled(false)
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
        tasks.withType<KotlinCompile>() {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_1_8.toString()
            }
        }
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}