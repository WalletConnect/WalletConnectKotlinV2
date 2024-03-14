plugins {
    jacoco
}

private val fileFilter = mutableSetOf(
    "**/R.class",
    "**/R\$*.class",
    "**/*\$1*", // Jacoco can not handle several "$" in class name.
    "**/BuildConfig.*",
    "**/*\$ViewInjector*.*",
    "**/*\$ViewBinder*.*",
    "**/Manifest*.*",
    "**/*Component.*",
    "**/*BR.*",
    "**/*\$InjectAdapter.*",
    "**/*\$ModuleAdapter.*",
    "**/*\$ViewInjector*.*",
    "**/*\$Lambda$*.*", // Jacoco can not handle several "$" in class name.
    "**/*\$inlined$*.*" // Kotlin specific, Jacoco can not handle several "$" in class name.
)
private val sourceDirectoriesTree = fileTree("${project.projectDir}") {
    include(
        "src/main/**",
        "src/debug/**",
    )
}
private val classDirectoriesTree = fileTree("${project.buildDir}") {
    include(
        "intermediates/javac/debug/classes/**",
        "tmp/kotlin-classes/debug/**"
    )
    exclude(fileFilter)
}
private val executionDataTree = fileTree(project.buildDir) {
    include(
        "jacoco/testDebugUnitTest.exec"
    )
}

tasks {
    register<JacocoReport>("jacocoDebugTestReport") {
        group = "Reporting"
        description = "Code coverage report for both Android and Unit tests."

        dependsOn("testDebugUnitTest")
        reports {
            xml.required.set(true)
            html.required.set(true)
            html.outputLocation.set(file("${buildDir}/reports/jacoco/html"))
            xml.outputLocation.set(file("${buildDir}/reports/jacoco/xml/jacoco.xml"))
        }
        sourceDirectories.setFrom(sourceDirectoriesTree)
        classDirectories.setFrom(files(classDirectoriesTree))
        executionData.setFrom(executionDataTree)
    }
}

afterEvaluate {
    project.tasks.getByName("testDebugUnitTest")
        .finalizedBy("jacocoDebugTestReport")
}