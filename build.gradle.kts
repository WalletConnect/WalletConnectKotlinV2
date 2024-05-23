import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

import org.sonarqube.gradle.SonarExtension

plugins {
    alias(libs.plugins.nexusPublish)
    alias(libs.plugins.sonarqube)
    id("release-scripts")
    id("version-bump")
}

allprojects {
    println("kobe: main KotlinCompile")

    tasks.withType<KotlinCompile>().configureEach {

        println("kobe: main configureEach")
        kotlinOptions {
            jvmTarget = jvmVersion.toString()
        }

    }

    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "androidx.navigation" && requested.name == "navigation-compose") {
                useVersion(libs.versions.androidxNavigation.get())
            }
            if (requested.group == "org.bouncycastle" && requested.name == "bcprov-jdk15on") {
                useTarget(libs.bouncyCastle)
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
                "sonar.gradle.skipCompile" to true,
                "sonar.coverage.exclusions" to "sample/**,**/di/**"
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
                    "sonar.java.binaries" to layout.buildDirectory,
                    "sonar.coverage.jacoco.xmlReportPaths" to "${layout.buildDirectory}/reports/jacoco/xml/jacoco.xml"
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

                dependencies {
                    add("testImplementation", libs.mockk)
                    add("testImplementation", libs.jUnit)
                    add("testRuntimeOnly", libs.jUnit.engine)
                }
            }
        }

        plugins.withId(rootProject.libs.plugins.javaLibrary.get().pluginId) {
            dependencies {
                add("testImplementation", libs.mockk)
                add("testImplementation", libs.jUnit)
                add("testRuntimeOnly", libs.jUnit.engine)
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

task<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
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