import com.android.build.gradle.BaseExtension
import org.sonarqube.gradle.SonarExtension

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("release-scripts")
    id("version-bump")
    alias(libs.plugins.sonarqube)
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

    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "androidx.navigation" && requested.name == "navigation-compose") {
                useVersion(composeNavigationVersion)
            }
        }
    }
}

sonar {
    properties {
        properties(
            mapOf(
                "sonar.projectKey" to "WalletConnect_WalletConnectKotlinV2",
                "sonar.organization" to "walletconnect",
                "sonar.host.url" to "https://sonarcloud.io",
                "sonar.gradle.skipCompile" to true
            )
        )
    }
}

subprojects {
    apply(plugin = rootProject.libs.plugins.sonarqube.get().pluginId)

    extensions.configure<SonarExtension> {
        setAndroidVariant("debug")

        isSkipProject = name == "bom"
        properties {
            properties(
                mapOf(
                    "sonar.gradle.skipCompile" to true,
                    "sonar.sources" to "${projectDir}/src/main/kotlin",
                    "sonar.java.binaries" to buildDir,
                    "sonar.coverage.jacoco.xmlReportPaths" to "${buildDir}/reports/jacoco/xml/jacoco.xml"
                )
            )
        }
    }

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