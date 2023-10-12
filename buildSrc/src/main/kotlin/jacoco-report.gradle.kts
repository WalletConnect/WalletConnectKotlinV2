import com.android.build.gradle.BaseExtension

plugins {
    jacoco
}

private val fileFilter = mutableSetOf(
    "**/R.class",
    "**/R\$*.class",
    "**/*\$1*", // Jacoco can not handle several "$" in class name.
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "android/**/*.*",
    "**/models/**",
    "**/*\$Lambda$*.*", // Jacoco can not handle several "$" in class name.
    "**/*\$inlined$*.*" // Kotlin specific, Jacoco can not handle several "$" in class name.
)


val javaTree = fileTree("dir" to "${project.buildDir}/intermediates/javac/debug/classes", "excludes" to fileFilter)
val kotlinTree = fileTree("dir" to "${project.buildDir}/tmp/kotlin-classes/debug", "excludes" to fileFilter)
private val classDirectoriesTree = fileTree("${project.buildDir}") {
    include(
        "**/classes/**/main/**",
        "**/intermediates/classes/debug/**",
        "**/intermediates/javac/debug/classes/**", // Android Gradle Plugin 3.2.x support.
        "**/tmp/kotlin-classes/debug/**"
    )
    exclude(fileFilter)
}

private val sourceDirectoriesTree = fileTree("${project.projectDir}") {
    include(
        "src/main/java/**",
        "src/main/kotlin/**",
        "src/debug/kotlin/**",
        "src/release/kotlin/**",
    )
}

private val executionDataTree = fileTree(project.buildDir) {
    include(
        "outputs/code_coverage/**/*.ec",
        "jacoco/jacocoTestReportDebug.exec",
        "jacoco/testDebugUnitTest.exec",
        "jacoco/test.exec"
    )
}

fun JacocoReportsContainer.reports() {
    xml.required.set(false)
    html.required.set(true)
    html.outputLocation.set(file("${buildDir}/reports/jacoco/html"))
}

fun JacocoCoverageVerification.setDirectories() {
    sourceDirectories.setFrom(sourceDirectoriesTree)
    classDirectories.setFrom(classDirectoriesTree)
    executionData.setFrom(executionDataTree)
}

fun JacocoReport.setDirectories() {
    sourceDirectories.setFrom(sourceDirectoriesTree)
    classDirectories.setFrom(classDirectoriesTree)
    executionData.setFrom(executionDataTree)
}

tasks {
    register<JacocoReport>("jacocoAndroidTestReport") {
        group = "Verification"
        description = "Code coverage report for both Android and Unit tests."
        dependsOn("testDebugUnitTest"/*, "createDebugUnitTestCoverageReport"*/)
        reports {
            reports()
        }
        setDirectories()
    }

    register<JacocoCoverageVerification>("jacocoAndroidCoverageVerification") {
        group = "Verification"
        description = "Code coverage verification for Android both Android and Unit tests."
        dependsOn("testDebugUnitTest", "jacocoAndroidTestReport"/*, "createDebugUnitTestCoverageReport"*/)
        violationRules {
            rule {
                element = "CLASS"
                excludes = listOf(
                    "**.FactorFacade.Builder",
                    "**.ServiceFacade.Builder",
                    "**.ChallengeFacade.Builder",
                    "**.Task"
                )
                limit {
                    minimum = "0.0".toBigDecimal()
                }
            }
        }
        setDirectories()
    }
}

(project as? ExtensionAware)?.extensions?.configure<BaseExtension>("android") {
    buildTypes.forEach { builtType ->
        builtType.enableUnitTestCoverage = false
    }
}

afterEvaluate {
    project.tasks.getByName("testDebugUnitTest").finalizedBy("jacocoAndroidTestReport", "jacocoAndroidCoverageVerification")
}