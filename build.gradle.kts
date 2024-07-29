import com.android.build.gradle.BaseExtension
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.sonarqube.gradle.SonarExtension
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory

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

val nexusUsername: String get() = System.getenv("OSSRH_USERNAME")
val nexusPassword: String get() = System.getenv("OSSRH_PASSWORD")
val nexusUrl = "https://s01.oss.sonatype.org/service/local/staging"

tasks.register("closeAndReleaseMultipleRepositories") {
    group = "release"
    description = "Release all Sonatype staging repositories"

    doLast {
        val repos = fetchOpenRepositoryIds()
        if (repos.isEmpty()) {
            println("No open repositories found")
            return@doLast
        }
        closeRepositories(repos)
        waitForAllRepositoriesToDesireState(repos, "closed")
        releaseRepositories(repos)
        waitForAllRepositoriesToDesireState(repos, "released")
        dropRepositories(repos)
        waitForArtifactsToBeAvailable()
    }
}

fun fetchOpenRepositoryIds(): List<String> {
    val client = HttpClients.createDefault()
    val httpGet = HttpGet("$nexusUrl/profile_repositories").apply {
        setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString("$nexusUsername:$nexusPassword".toByteArray()))
    }

    val response = client.execute(httpGet)
    val responseBody = EntityUtils.toString(response.entity)
    if (response.statusLine.statusCode != 200) {
        throw RuntimeException("Failed: HTTP error code : ${response.statusLine.statusCode} $responseBody")
    }

    return parseRepositoryIds(responseBody)
}

fun parseRepositoryIds(xmlResponse: String): List<String> {
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val inputStream = xmlResponse.byteInputStream()
    val doc = builder.parse(inputStream)
    val nodeList = doc.getElementsByTagName("stagingProfileRepository")

    val repositoryIds = mutableListOf<String>()
    for (i in 0 until nodeList.length) {
        val node = nodeList.item(i)
        val element = node as? org.w3c.dom.Element
        val repoId = element?.getElementsByTagName("repositoryId")?.item(0)?.textContent
        val type = element?.getElementsByTagName("type")?.item(0)?.textContent
        if (repoId != null && type == "open") {
            repositoryIds.add(repoId)
        }
    }
    return repositoryIds
}

fun closeRepositories(repoIds: List<String>) {
    val closeUrl = "$nexusUrl/bulk/close"
    val json = """
            {
                "data": {
                    "stagedRepositoryIds": ${repoIds.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")}
                }
            }
        """.trimIndent()
    executePostRequest(closeUrl, json)
    println("Closed repositories: ${repoIds.joinToString(", ")}")
}

fun releaseRepositories(repoIds: List<String>) {
    val releaseUrl = "$nexusUrl/bulk/promote"
    val json = """
            {
                "data": {
                    "stagedRepositoryIds": ${repoIds.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")}
                }
            }
        """.trimIndent()
    println("Released repositories: ${repoIds.joinToString(", ")}")
    executePostRequest(releaseUrl, json)
}

fun dropRepositories(repoIds: List<String>) {
    val dropUrl = "$nexusUrl/bulk/drop"
    val json = """
            {
                "data": {
                    "stagedRepositoryIds": ${repoIds.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")}
                }
            }
        """.trimIndent()
    executePostRequest(dropUrl, json)
    println("Dropped repositories: ${repoIds.joinToString(", ")}")
}

fun waitForAllRepositoriesToDesireState(repoIds: List<String>, desireState: String) {
    val client = HttpClients.createDefault()
    val statusUrl = "$nexusUrl/repository/"
    val closedRepos = mutableSetOf<String>()

    while (closedRepos.size < repoIds.size) {
        repoIds.forEach { repoId ->
            if (!closedRepos.contains(repoId)) {
                val httpGet = HttpGet("$statusUrl$repoId").apply {
                    setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString("$nexusUsername:$nexusPassword".toByteArray()))
                }
                val response = client.execute(httpGet)
                println("GET request to $repoId returned status code: ${response.statusLine.statusCode}")
                val responseBody = EntityUtils.toString(response.entity)

                val state = parseRepositoryState(responseBody, repoId)
                if (state == desireState) {
                    println("Repository $repoId is now in state: $state")
                    closedRepos.add(repoId)
                } else {
                    println("Waiting for repository $repoId to be $desireState, current state: $state")
                }
            }
        }
        if (closedRepos.size < repoIds.size) {
            Thread.sleep(30000) // Wait for 30 seconds before retrying
        }
    }
}

fun waitForArtifactsToBeAvailable() {
    val repoIds: List<String> = repoIdWithVersion.map { it.first }
    val client = HttpClients.createDefault()
    val artifactUrls = repoIdWithVersion.map { (repoId, version) ->
        println("Checking: https://repo1.maven.org/maven2/com/walletconnect/$repoId/$version/")
        "https://repo1.maven.org/maven2/com/walletconnect/$repoId/$version/"
    }
    val maxRetries = 40
    var attempt = 0
    val availableRepos = mutableSetOf<String>()

    while (availableRepos.size < repoIds.size && attempt < maxRetries) {
        artifactUrls.forEachIndexed { index, artifactUrl ->
            if (!availableRepos.contains(repoIds[index])) {
                val httpGet = HttpGet(artifactUrl)
                try {
                    val response = client.execute(httpGet)
                    val statusCode = response.statusLine.statusCode
                    EntityUtils.consume(response.entity) // Ensure the response is fully consumed
                    if (statusCode == 200 || statusCode == 201) {
                        println("Artifact for repository ${repoIds[index]} is now available.")
                        availableRepos.add(repoIds[index])
                    } else {
                        println("Artifact for repository ${repoIds[index]} not yet available. Status code: $statusCode")
                    }
                } catch (e: Exception) {
                    println("Error checking artifact for repository ${repoIds[index]}: ${e.message}")
                } finally {
                    httpGet.releaseConnection() // Ensure the connection is released
                }
            }
        }
        if (availableRepos.size < repoIds.size) {
            println("Waiting for artifacts to be available... Attempt: ${attempt + 1}")
            attempt++
            Thread.sleep(45000) // Wait for 10 seconds before retrying
        }
    }

    if (availableRepos.size < repoIds.size) {
        throw RuntimeException("Artifacts were not available after ${maxRetries * 10} seconds.")
    } else {
        println("All artifacts are now available.")
    }
}

fun executePostRequest(url: String, json: String) {
    val client = HttpClients.createDefault()
    val httpPost = HttpPost(url).apply {
        setHeader("Content-type", "application/json")
        entity = StringEntity(json)
        setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString("$nexusUsername:$nexusPassword".toByteArray()))
    }

    val response = client.execute(httpPost)
    val responseBody = EntityUtils.toString(response.entity)
    if (response.statusLine.statusCode != 201) {
        throw RuntimeException("Failed: HTTP error code : ${response.statusLine.statusCode} $responseBody")
    }
}

fun parseRepositoryState(xmlResponse: String, repositoryId: String): String? {
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val inputStream = xmlResponse.byteInputStream()
    val doc = builder.parse(inputStream)
    val nodeList = doc.getElementsByTagName("stagingProfileRepository")

    for (i in 0 until nodeList.length) {
        val node = nodeList.item(i)
        val element = node as? org.w3c.dom.Element
        val repoId = element?.getElementsByTagName("repositoryId")?.item(0)?.textContent
        if (repoId == repositoryId) {
            return element.getElementsByTagName("type").item(0)?.textContent
        }
    }
    return null
}

tasks.register<Exec>("createTag") {
    val tagName = "BOM_$BOM_VERSION"
    commandLine("git", "tag", tagName)
}

tasks.register<Exec>("pushTagToMain") {
    val tagName = "BOM_$BOM_VERSION"
    val repoUrl = "https://github.com/WalletConnect/WalletConnectKotlinV2.git"
    val token = System.getenv("GITHUB_TOKEN") ?: throw GradleException("GITHUB_TOKEN environment variable is not set")
    dependsOn("createTag")
    val authenticatedRepoUrl = repoUrl.replace("https://", "https://$token:@")
    commandLine("git", "push", authenticatedRepoUrl, tagName, "refs/heads/main")
}

private val repoIdWithVersion = listOf(
    Pair(ANDROID_BOM, BOM_VERSION),
    Pair(FOUNDATION, FOUNDATION_VERSION),
    Pair(ANDROID_CORE, CORE_VERSION),
    Pair(SIGN, SIGN_VERSION),
    Pair(AUTH, AUTH_VERSION),
    Pair(CHAT, CHAT_VERSION),
    Pair(NOTIFY, NOTIFY_VERSION),
    Pair(WEB_3_WALLET, WEB_3_WALLET_VERSION),
    Pair(WEB_3_MODAL, WEB_3_MODAL_VERSION),
    Pair(WC_MODAL, WC_MODAL_VERSION),
    Pair(MODAL_CORE, MODAL_CORE_VERSION)
)