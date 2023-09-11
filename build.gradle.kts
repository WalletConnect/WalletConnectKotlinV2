import com.android.build.gradle.BaseExtension
import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("release-scripts")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
}

buildscript {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:$agpVersion")
        classpath("org.jetbrains.dokka:dokka-core:$dokkaVersion")      // TODO: Leave version until AGP 7.3 https://github.com/Kotlin/dokka/issues/2472#issuecomment-1143604232
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")
        classpath("com.squareup.sqldelight:gradle-plugin:$sqlDelightVersion")
        classpath("com.google.gms:google-services:$googleServiceVersion")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.2")
        classpath("com.google.firebase:firebase-appdistribution-gradle:4.0.0")
    }
}

allprojects {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
        jcenter() // Warning: this repository is going to shut down soon
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = jvmVersion.toString()
        }
    }
}

subprojects {
    afterEvaluate {
        if (hasProperty("android")) {
            extensions.configure(BaseExtension::class.java) {
                packagingOptions {
                    with(resources.excludes) {
                        add("META-INF/INDEX.LIST")
                        add("META-INF/DEPENDENCIES")
                        add("META-INF/LICENSE.md")
                        add("META-INF/NOTICE.md")
                    }
                }
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

// This task is used to copy the google-services.json file to all sample modules except modals
tasks.register("copyGoogleServicesToAllSample") {
    val googleServicesJsonFileName = "google-services.json"
    val googleServicesFile = rootProject.file("sample/common/$googleServicesJsonFileName")
    val listOfDirectoriesToIgnore = listOf("common", "modals")

    rootProject.file("sample").listFiles()?.forEach { sampleDir ->
        if (sampleDir.name !in listOfDirectoriesToIgnore && sampleDir.isDirectory) {
            val destination = sampleDir.resolve(googleServicesJsonFileName)

            if (!destination.exists()) {
                googleServicesFile.copyTo(destination)
            }
        }
    }
}

idea.project.settings.taskTriggers {
    beforeSync(tasks.getByName("copyGoogleServicesToAllSample"))
}

nexusPublishing {
    repositories {
//        project.version = "-SNAPSHOT"
        sonatype {
            stagingProfileId.set(System.getenv("SONATYPE_STAGING_PROFILE_ID"))
            username.set(System.getenv("OSSRH_USERNAME"))
            password.set(System.getenv("OSSRH_PASSWORD"))
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}