
import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.sonarqube.gradle.SonarExtension
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

plugins {
    alias(libs.plugins.nexusPublish)
    alias(libs.plugins.sonarqube)
    id("release-scripts")
    id("version-bump")
}

allprojects {
    tasks.withType<KotlinCompile>().configureEach {
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
                "sonar.coverage.exclusions" to "sample/**,**/di/**,/buildSrc/**,**/gradle/**,**/test/**,**/androidTest/**,**/build.gradle.kts",
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

tasks.register("closeSonatypeStagingRepositories") {
    group = "release"
    description = "Close all Sonatype staging repositories"

    doLast {
        val client = HttpClients.createDefault()
        val get = HttpGet("https://s01.oss.sonatype.org/service/local/staging/profile_repositories")
        get.setHeader("Content-Type", "application/xml")
        get.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString("${System.getenv("OSSRH_USERNAME")}:${System.getenv("OSSRH_PASSWORD")}".toByteArray()))

        val response: HttpResponse
        try {
            response = client.execute(get)
        } catch (e: Exception) {
            println("Failed to fetch staging repositories - Exception: ${e.message}")
            return@doLast
        }

        if (response.statusLine.statusCode != 200) {
            println("Failed to list staging repositories - Response Code: ${response.statusLine.statusCode}")
            return@doLast
        }

        val xmlResponse = EntityUtils.toString(response.entity).trim()
        val repositoryIds = mutableListOf<String>()
        try {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(xmlResponse.byteInputStream())

            doc.documentElement.normalize()

            val repositories = doc.getElementsByTagName("stagingProfileRepository")
            for (i in 0 until repositories.length) {
                val repository = repositories.item(i)

                if (repository.nodeType == org.w3c.dom.Node.ELEMENT_NODE) {
                    val element = repository as org.w3c.dom.Element
                    val repositoryId = element.getElementsByTagName("repositoryId").item(0).textContent.trim()
                    val type = element.getElementsByTagName("type").item(0).textContent.trim()

                    if (type == "open") {
                        repositoryIds.add(repositoryId)
                    }
                }
            }
        } catch (e: Exception) {
            println("Failed to parse XML response - Exception: ${e.message}")
            return@doLast
        }

        if (repositoryIds.isEmpty()) {
            println("No open staging repositories found")
            return@doLast
        }

        val post = HttpPost("https://s01.oss.sonatype.org/service/local/staging/bulk/close")
        post.setHeader("Content-Type", "application/json")
        post.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString("${System.getenv("OSSRH_USERNAME")}:${System.getenv("OSSRH_PASSWORD")}".toByteArray()))

        val json = """
            {
                "data": {
                    "stagedRepositoryIds": ${repositoryIds.joinToString(prefix = "[", postfix = "]", separator = ",") { "\"$it\"" }}
                }
            }
        """.trimIndent()

        post.entity = StringEntity(json)

        try {
            val closeResponse = client.execute(post)
            println("Closed staging repositories - Response Code: ${closeResponse.statusLine.statusCode}")
        } catch (e: Exception) {
            println("Failed to close staging repositories - Exception: ${e.message}")
        }
    }
}

//tasks.register("releaseSonatypeStagingRepositories", Exec::class) {
//    group = "release"
//    description = "Release all closed Sonatype staging repositories"
//
//    // Command to release all closed staging repositories
//    commandLine("curl", "-u", "${System.getenv("OSSRH_USERNAME")}:${System.getenv("OSSRH_PASSWORD")}", "-X", "POST", "https://oss.sonatype.org/service/local/staging/bulk/promote")
//}