import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.io.File

const val RESOURCE_FOLDER_PATH = "src/test/resources"
const val VERSION_FILE_PATH = "$RESOURCE_FOLDER_PATH/example/Example_Versions.txt"
const val README_FILE_PATH = "$RESOURCE_FOLDER_PATH/example/Example_Readme.md"



// Not sure why but this has to be run in terminal with ./gradlew buildSrc:test
// Added test that fails to see if it actually fails on wrong tests
internal class VersionBumpTest {


    @Test
    fun testThatFails() {
        assertEquals(1, 2)
    }

    // Tests how the script works in release mode, when foundation is changed
    @Test
    fun releaseFoundationBumpTest() {
        val result = bumpVersions(mapOf("modules" to "foundation"), VersionBumpType.RELEASE, InputType.AUTOMATIC, VERSION_FILE_PATH, README_FILE_PATH)

        val expectedReadmeText = File("$RESOURCE_FOLDER_PATH/releaseFoundationResult/Result_Readme.md").readText()
        val expectedVersionsText = File("$RESOURCE_FOLDER_PATH/releaseFoundationResult/Result_Versions.txt").readText()

        assertEquals(expectedReadmeText, result.readmeFileText)
        assertEquals(expectedVersionsText, result.versionFileText)
    }

    // Tests how the script works in fix mode, when core is changed
    @Test
    fun fixCoreBumpTest() {
        val result = bumpVersions(mapOf("modules" to "core"), VersionBumpType.FIX, InputType.AUTOMATIC, VERSION_FILE_PATH, README_FILE_PATH)

        val expectedReadmeText = File("$RESOURCE_FOLDER_PATH/fixCoreResult/Result_Readme.md").readText()
        val expectedVersionsText = File("$RESOURCE_FOLDER_PATH/fixCoreResult/Result_Versions.txt").readText()

        assertEquals(expectedReadmeText, result.readmeFileText)
        assertEquals(expectedVersionsText, result.versionFileText)
    }

    // Tests how the script works in fix mode, when core is changed
    @Test
    fun releaseModalCoreBumpTest() {
        val result = bumpVersions(mapOf("modules" to "modal_core"), VersionBumpType.RELEASE, InputType.AUTOMATIC, VERSION_FILE_PATH, README_FILE_PATH)

        val expectedReadmeText = File("$RESOURCE_FOLDER_PATH/releaseModalCoreResult/Result_Readme.md").readText()
        val expectedVersionsText = File("$RESOURCE_FOLDER_PATH/releaseModalCoreResult/Result_Versions.txt").readText()

        assertEquals(expectedReadmeText, result.readmeFileText)
        assertEquals(expectedVersionsText, result.versionFileText)
    }

    // Tests how the script works in fix mode, when only auth module is changed
    @Test
    fun fixOnlyAuthBumpTest() {
        val result = bumpVersions(mapOf("modules" to "auth"), VersionBumpType.FIX, InputType.AUTOMATIC, VERSION_FILE_PATH, README_FILE_PATH)

        val expectedReadmeText = File("$RESOURCE_FOLDER_PATH/fixOnlyAuthResult/Result_Readme.md").readText()
        val expectedVersionsText = File("$RESOURCE_FOLDER_PATH/fixOnlyAuthResult/Result_Versions.txt").readText()

        assertEquals(expectedReadmeText, result.readmeFileText)
        assertEquals(expectedVersionsText, result.versionFileText)
    }

    // Tests how the script works in fix mode, when only sign module is changed
    @Test
    fun fixOnlySignBumpTest() {
        val result = bumpVersions(mapOf("modules" to "sign"), VersionBumpType.FIX, InputType.AUTOMATIC, VERSION_FILE_PATH, README_FILE_PATH)

        val expectedReadmeText = File("$RESOURCE_FOLDER_PATH/fixOnlySignResult/Result_Readme.md").readText()
        val expectedVersionsText = File("$RESOURCE_FOLDER_PATH/fixOnlySignResult/Result_Versions.txt").readText()

        assertEquals(expectedReadmeText, result.readmeFileText)
        assertEquals(expectedVersionsText, result.versionFileText)
    }


    // Tests how the script works in fix mode, when only auth and w3m modules are changed
    @Test
    fun fixSignAndModalCoreBumpTest() {
        val result = bumpVersions(mapOf("modules" to "sign,modal_core"), VersionBumpType.FIX, InputType.AUTOMATIC, VERSION_FILE_PATH, README_FILE_PATH)

        val expectedReadmeText = File("$RESOURCE_FOLDER_PATH/fixSignAndModalCoreResult/Result_Readme.md").readText()
        val expectedVersionsText = File("$RESOURCE_FOLDER_PATH/fixSignAndModalCoreResult/Result_Versions.txt").readText()

        assertEquals(expectedReadmeText, result.readmeFileText)
        assertEquals(expectedVersionsText, result.versionFileText)
    }


    // Tests how the replaceChartVersion works
    @Test
    fun replaceChartVersionTest() {
        val versions = mapOf(Version.BOM to true, Version.CORE to true)

        // Example row from versions file
        var newFirstRow = "| 1.21.0                                                                                  | 1.26.0                   | 2.24.0                    | 1.24.0        " +
                "            | 1.0.0-beta23              | 1.0.0-beta04                  | 1.19.0                           | 1.1.0                          | 1.1.0                                 " +
                "           |\n"

        versions.filter { it.value }.forEach { (version, _) ->
            newFirstRow = when (version){
                Version.BOM -> newFirstRow.replaceChartVersion(version, "1.21.0", "1.22.0")
                Version.CORE -> newFirstRow.replaceChartVersion(version, "1.26.0", "1.27.0")
                else -> newFirstRow
            }
        }

        val expectedRow = "| 1.22.0                                                                                  | 1.27.0                   | 2.24.0                    | 1.24.0        " +
                "            | 1.0.0-beta23              | 1.0.0-beta04                  | 1.19.0                           | 1.1.0                          | 1.1.0                                 " +
                "           |\n"

        assertEquals(expectedRow, newFirstRow)
    }
}