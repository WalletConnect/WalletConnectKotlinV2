import com.android.build.api.dsl.ManagedVirtualDevice
import com.android.build.api.dsl.TestOptions
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke

fun TestOptions.registerManagedDevices(moduleName: String) {
    val githubRunId = System.getenv("GITHUB_RUN_ID") ?: ""
    val prefix = if (githubRunId.isEmpty()) moduleName else "${githubRunId}_$moduleName"
    managedDevices {
        devices {
            create<ManagedVirtualDevice>("${prefix}Google32") {
                device = "Nexus 6"
                apiLevel = 32
                systemImageSource = "google"
            }
            create<ManagedVirtualDevice>("${prefix}Google27") {
                device = "Nexus 6"
                apiLevel = 27
                systemImageSource = "google"
            }
        }
    }
}