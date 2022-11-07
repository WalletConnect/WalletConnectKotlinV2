import com.android.build.gradle.BaseExtension
import com.android.builder.model.JavaArtifact

plugins {
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

tasks {
    register("javadocJar", Jar::class) {
        dependsOn(named("dokkaHtml"))
        archiveClassifier.set("javadoc")
        from("$buildDir/dokka/html")
    }
    register("sourceJar", Jar::class) {
        archiveClassifier.set("sources")
        from(((project as ExtensionAware).extensions.getByName("sourceSets") as SourceSetContainer).getByName("main").allSource)
    }
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("mavenJvm") {
                from(components["java"])
                artifact(tasks.getByName("sourceJar"))
                artifact(tasks.getByName("javadocJar"))

                groupId = "com.walletconnect"
                artifactId = requireNotNull(extra.get(KEY_PUBLISH_ARTIFACT_ID)).toString()
                version = requireNotNull(extra.get(KEY_PUBLISH_VERSION)).toString()

                pom {
                    name.set("WalletConnect ${requireNotNull(extra.get(KEY_SDK_NAME))}")
                    description.set("${requireNotNull(extra.get(KEY_SDK_NAME))}")
                    url.set("https://github.com/WalletConnect/WalletConnectKotlinV2")

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