plugins {
    id("com.android.library")
    kotlin("android")
    id("publish-module-android")
}

project.apply {
    extra[KEY_PUBLISH_ARTIFACT_ID] = "android-bom"
    extra[KEY_PUBLISH_VERSION] = CORE_VERSION
    extra[KEY_SDK_NAME] = "Android BOM"
}

android {
    compileSdk = COMPILE_SDK

    defaultConfig {
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    }
}

//afterEvaluate {
//    publishing {
//        publications {
//            register<MavenPublication>("bom") {
//                from(components["release"])
//                artifact(tasks.getByName("javadocJar"))
//                artifact(tasks.getByName("sourcesJar"))
//
////                group = "com.walletconnect"
//                groupId = "com.walletconnect"
////                artifactId = "android-core-bom"
//                artifactId = requireNotNull(project.extra[KEY_PUBLISH_ARTIFACT_ID]).toString()
//                version = requireNotNull(project.extra[KEY_PUBLISH_VERSION]).toString().also { println(it) }
//
//                pom {
//                    name.set("WalletConnect ${requireNotNull(extra.get(KEY_SDK_NAME))}")
//                    description.set("${requireNotNull(extra.get(KEY_SDK_NAME))} SDK for WalletConnect")
//                    url.set("https://github.com/WalletConnect/WalletConnectKotlinV2")
//
//                    licenses {
//                        license {
//                            name.set("The Apache License, Version 2.0")
//                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
//                        }
//                        license {
//                            name.set("SQLCipher Community Edition")
//                            url.set("https://www.zetetic.net/sqlcipher/license/")
//                        }
//                    }
//
//                    developers {
//                        developer {
//                            id.set("KotlinSDKTeam")
//                            name.set("WalletConnect Kotlin")
//                            email.set("Kotlin@WalletConnect.com")
//                        }
//                    }
//
//                    scm {
//                        connection.set("scm:git:git://github.com/WalletConnect/WalletConnectKotlinV2.git")
//                        developerConnection.set("scm:git:ssh://github.com/WalletConnect/WalletConnectKotlinV2.git")
//                        url.set("https://github.com/WalletConnect/WalletConnectKotlinV2")
//                    }
//                }
//            }
//        }
//    }
//}

dependencies {
    constraints {
//        implementation("com.walletconnect:foundation:1.1.0")
        implementation(project(":foundation"))
        implementation(project(":androidCore:sdk"))
        implementation(project(":androidCore:impl"))
        implementation(project(":sign:sdk"))
        implementation(project(":auth:sdk"))
        implementation(project(":chat:sdk"))
    }
}

//fun Project.setupLibraryModule(
//    name: String?,
//    buildConfig: Boolean = false,
//    publish: Boolean = false,
//    document: Boolean = publish,
//) = setupBaseModule<LibraryExtension>(name) {
//    libraryVariants.all {
//        generateBuildConfigProvider?.configure { enabled = buildConfig }
//    }
//    if (publish) {
//        if (document) apply(plugin = "org.jetbrains.dokka")
//        apply(plugin = "com.vanniktech.maven.publish.base")
//        publishing {
//            singleVariant("release") {
//                withJavadocJar()
//                withSourcesJar()
//            }
//        }
//        afterEvaluate {
//            extensions.configure<PublishingExtension> {
//                publications.create<MavenPublication>("release") {
//                    from(components["release"])
//                    // https://github.com/vanniktech/gradle-maven-publish-plugin/issues/326
//                    val id = project.property("POM_ARTIFACT_ID").toString()
//                    artifactId = artifactId.replace(project.name, id)
//                }
//            }
//        }
//    }
//}
//
//inline fun <reified T : com.android.build.gradle.BaseExtension> Project.setupBaseModule(
//    name: String?,
//    crossinline block: T.() -> Unit = {}
//) = extensions.configure<T>("android") {
//    namespace = name
//    compileSdkVersion(project.compileSdk)
//    defaultConfig {
//        minSdk = project.minSdk
//        targetSdk = project.targetSdk
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
//    }
//    kotlinOptions {
//        jvmTarget = "1.8"
//        allWarningsAsErrors = true
//
//        val arguments = mutableListOf(
//            // https://kotlinlang.org/docs/compiler-reference.html#progressive
//            "-progressive",
//            // Enable Java default method generation.
//            "-Xjvm-default=all",
//            // Generate smaller bytecode by not generating runtime not-null assertions.
//            "-Xno-call-assertions",
//            "-Xno-param-assertions",
//            "-Xno-receiver-assertions",
//        )
//        if (project.name != "coil-test") {
//            arguments += "-opt-in=coil.annotation.ExperimentalCoilApi"
//        }
//        // https://youtrack.jetbrains.com/issue/KT-41985
//        freeCompilerArgs += arguments
//    }
//    packagingOptions {
//        resources.pickFirsts += arrayOf(
//            "META-INF/AL2.0",
//            "META-INF/LGPL2.1",
//            "META-INF/*kotlin_module",
//        )
//    }
//    testOptions {
//        unitTests.isIncludeAndroidResources = true
//    }
//    lint {
//        warningsAsErrors = true
//        disable += arrayOf(
//            "UnusedResources",
//            "VectorPath",
//            "VectorRaster",
//        )
//    }
//    block()
//}

//signing {
//    useInMemoryPgpKeys(
//        System.getenv("SIGNING_KEY_ID"),
//        System.getenv("SIGNING_KEY"),
//        System.getenv("SIGNING_PASSWORD")
//    )
//    sign(publishing.publications)
//}