import org.apache.tools.ant.taskdefs.condition.Os
import kotlin.reflect.full.safeCast

// Example ./gradlew releaseAllSDKs -Ptype=local
// Example ./gradlew releaseAllSDKs -Ptype=sonatype
tasks.register("releaseAllSDKs") {
    doLast {
        project.findProperty("type")
            ?.run(String::class::safeCast)
            ?.run {
                println("Converting parameter to an supported ReleaseType value")
                ReleaseType.valueOf(this.toUpperCase())
            }?.let { releaseType ->
                generateListOfModuleTasks(releaseType).forEach { task ->
                    println("Executing Task: $task")
                    exec {
                        val gradleCommand = if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                            "gradlew.bat"
                        } else {
                            "./gradlew"
                        }
                        commandLine(gradleCommand, task.path)
                    }
                }
            } ?: throw Exception("Missing Type parameter")
    }
}

fun generateListOfModuleTasks(type: ReleaseType): List<Task> = compileListOfSDKs().extractListOfPublishingTasks(type)

// Triple consists of the root module name, the child module name, and if it's a JVM or Android module
fun compileListOfSDKs(): List<Triple<String, String?, String>> = mutableListOf(
    Triple("foundation", null, "jvm"),
    Triple("androidCore", "sdk", "android"),
    Triple("sign", "sdk", "android"),
    Triple("auth", "sdk", "android"),
    Triple("chat", "sdk", "android"),
    Triple("push", "sdk", "android"),
    Triple("web3", "wallet", "android"),
    Triple("web3", "inbox", "android"),
    Triple("core", "modalCore", "android"),
    Triple("product", "modal", "android"),
).apply {
    // The BOM has to be last artifact
    add(Triple("androidCore", "bom", "jvm"))
}

// This extension function will determine which task to run based on the type passed
fun List<Triple<String, String?, String>>.extractListOfPublishingTasks(type: ReleaseType): List<Task> = map { (parentModule, childModule, env) ->
    val task = when {
        env == "jvm" && type == ReleaseType.LOCAL -> "${publishJvmRoot}MavenLocal"
        env == "jvm" && type == ReleaseType.SONATYPE -> "${publishJvmRoot}SonatypeRepository"
        env == "android" && type == ReleaseType.LOCAL -> "${publishAndroidRoot}MavenLocal"
        env == "android" && type == ReleaseType.SONATYPE -> "${publishAndroidRoot}SonatypeRepository"
        else -> throw Exception("Unknown Type or Env")
    }

    val module = if (childModule != null) {
        subprojects.first { it.name == parentModule }.subprojects.first { it.name == childModule }
    } else {
        subprojects.first { it.name == parentModule }
    }

    module.tasks.getByName(task)
}

private val publishJvmRoot = "publishMavenJvmPublicationTo"
private val publishAndroidRoot = "publishReleasePublicationTo"

enum class ReleaseType {
    LOCAL, SONATYPE
}