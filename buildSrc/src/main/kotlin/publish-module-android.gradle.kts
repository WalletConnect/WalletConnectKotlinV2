import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.api.DefaultAndroidSourceDirectorySet

plugins {
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

tasks {
    register("javadocJar", Jar::class) {
        dependsOn(named("dokkaHtml"))
        archiveClassifier.set("javadoc")
        println("kobe: android javaDocs")
        from("${layout.buildDirectory}/dokka/html")
    }

    register("sourcesJar", Jar::class) {
        archiveClassifier.set("sources")
        println("kobe: android sourcesJar")
        from(
            (project.extensions.getByType<BaseExtension>().sourceSets.getByName("main").kotlin.srcDirs("kotlin") as DefaultAndroidSourceDirectorySet).srcDirs,
            (project.extensions.getByType<BaseExtension>().sourceSets.getByName("release").kotlin.srcDirs("kotlin") as DefaultAndroidSourceDirectorySet).srcDirs
        )
    }
}

(project as ExtensionAware).extensions.configure<LibraryExtension>("android") {
    publishing.singleVariant("release")
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                afterEvaluate { from(components["release"]) }
                artifact(tasks.getByName("javadocJar"))
                artifact(tasks.getByName("sourcesJar"))

                groupId = "com.walletconnect"
                artifactId = requireNotNull(project.extra[KEY_PUBLISH_ARTIFACT_ID]).toString()
                version = requireNotNull(project.extra[KEY_PUBLISH_VERSION]).toString()

                println("kobe: android artefact: $artifactId")

                pom {
                    name.set("WalletConnect ${requireNotNull(extra.get(KEY_SDK_NAME))}")
                    description.set("${requireNotNull(extra.get(KEY_SDK_NAME))} SDK for WalletConnect")
                    url.set("https://github.com/WalletConnect/WalletConnectKotlinV2")
                    println("kobe: android pom")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                        license {
                            name.set("SQLCipher Community Edition")
                            url.set("https://www.zetetic.net/sqlcipher/license/")
                        }
                    }

                    developers {
                        developer {
                            id.set("KotlinSDKTeam")
                            name.set("WalletConnect Kotlin")
                            email.set("Kotlin@WalletConnect.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/WalletConnect/WalletConnectKotlinV2.git")
                        developerConnection.set("scm:git:ssh://github.com/WalletConnect/WalletConnectKotlinV2.git")
                        url.set("https://github.com/WalletConnect/WalletConnectKotlinV2")
                    }
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("SIGNING_KEY_ID"),
        System.getenv("SIGNING_KEY"),
        System.getenv("SIGNING_PASSWORD")
    )
    sign(publishing.publications)
}