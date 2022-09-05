plugins {
    id("com.android.library")
    kotlin("android")
    id("com.squareup.sqldelight")
    `maven-publish`
    id("com.google.devtools.ksp") version kspVersion
    id("org.jetbrains.dokka")
    id("publish-module")
}

project.apply {
    extra["PUBLISH_VERSION"] = "2.0.0-rc0-test"
    extra["PUBLISH_ARTIFACT_ID"] = "sign"
}

android {
    compileSdk = 32

    defaultConfig {
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK

        aarMetadata {
            minCompileSdk = MIN_SDK
            targetSdk = TARGET_SDK
        }

        buildConfigField(type = "String", name= "sdkVersion", value = "\"2.0.0-rc.3\"")
        testInstrumentationRunner = "com.walletconnect.sign.test.utils.WCTestRunner"
        testInstrumentationRunnerArguments += mutableMapOf("runnerBuilder" to "de.mannodermaus.junit5.AndroidJUnit5Builder")
        testInstrumentationRunnerArguments += mutableMapOf("clearPackageData" to "true")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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

sqldelight {
    database("Database") {
        packageName = "com.walletconnect.sign"
        dependency(project(":android_core"))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    register("javadocJar", Jar::class) {
        dependsOn(named("dokkaHtml"))
        archiveClassifier.set("javadoc")
        from("$buildDir/dokka/html")
    }

    register("sourcesJar", Jar::class) {
        archiveClassifier.set("sources")
        from(android.sourceSets.getByName("main").java.srcDirs)
    }
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("mavenAndroid") {
                afterEvaluate { artifact(tasks.getByName("bundleReleaseAar")) }
                artifact(tasks.getByName("javadocJar"))
                artifact(tasks.getByName("sourcesJar"))

                artifactId = "sign"

                pom {
                    name.set("WalletConnect Sign")
                    description.set("Sign SDK for WalletConnect.")
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

                    withXml {
                        fun groovy.util.Node.addDependency(dependency: Dependency, scope: String) {
                            appendNode("dependency").apply {
                                appendNode("groupId", dependency.group)
                                appendNode("artifactId", dependency.name)
                                appendNode("version", dependency.version)
                                appendNode("scope", scope)
                            }
                        }

                        asNode().appendNode("dependencies").let { dependencies ->
                            // List all "api" dependencies as "compile" dependencies
                            configurations.api.get().allDependencies.forEach {
                                dependencies.addDependency(it, "compile")
                            }
                            // List all "implementation" dependencies as "runtime" dependencies
                            configurations.implementation.get().allDependencies.forEach {
                                dependencies.addDependency(it, "runtime")
                            }
                        }
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

dependencies {
    api(project(":android_core"))

    moshiKsp()
    androidXTest()
    jUnit5()
    robolectric()
    mockk()
    testJson()
    coroutinesTest()
    scarletTest()
    sqlDelightTest()
}