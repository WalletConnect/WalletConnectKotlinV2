
import org.gradle.internal.impldep.org.apache.http.HttpResponse
import org.gradle.internal.impldep.org.apache.http.client.methods.HttpGet
import org.gradle.internal.impldep.org.apache.http.client.methods.HttpPost
import org.gradle.internal.impldep.org.apache.http.entity.StringEntity
import org.gradle.internal.impldep.org.apache.http.impl.client.HttpClients
import org.gradle.internal.impldep.org.apache.http.util.EntityUtils
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory

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