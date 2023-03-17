tasks.register("releaseLocal") {
    doLast {
        generateListOfModuleTasks("local")
            .map { it.path }
            .forEach { task ->
                println("Releasing $task")
                exec {
                    commandLine("./gradlew", task)
                }
            }
    }
}

tasks.register("releaseRemote") {
    doLast {
        generateListOfModuleTasks("release")
            .map { it.path }
            .forEach { task ->
                println("Releasing $task")
                exec {
                    commandLine("./gradlew", task)
                }
            }
    }
}

fun generateListOfModuleTasks(type: String): List<Task> = compileListOfSDKs().extractListOfPublishingTasks(type)

fun compileListOfSDKs(): List<Triple<String, String?, String>> = listOf(
    Triple("foundation", null, "jvm"),
    Triple("androidCore", "sdk", "android"),
    Triple("sign", "sdk", "android"),
    Triple("auth", "sdk", "android"),
//    Triple("chat", "sdk", "android"),
    Triple("web3", "wallet", "android"),
//    Triple("web3", "inbox", "android"),
    Triple("androidCore", "bom", "jvm"),
)

fun List<Triple<String, String?, String>>.extractListOfPublishingTasks(type: String): List<Task> = map { (parentModule, childModule, env) ->
    val task = when {
        env == "jvm" && type == "local" -> "publishMavenJvmPublicationToMavenLocal"
        env == "jvm" && type == "release" -> "publishMavenJvmPublicationToSonatypeRepository"
        env == "android" && type == "local" -> "publishReleasePublicationToMavenLocal"
        env == "android" && type == "release" -> "publishReleasePublicationToSonatypeRepository"
        else -> throw Exception("Unknown Type")
    }

    val module = if (childModule != null) {
        subprojects.first { it.name == parentModule }.subprojects.first { it.name == childModule }
    } else {
        subprojects.first { it.name == parentModule }
    }

    module.tasks.getByName(task)
}