import com.android.build.api.dsl.ManagedVirtualDevice
import com.android.build.api.dsl.TestOptions
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke

fun TestOptions.registerManagedDevices() {
    managedDevices {
        devices {
            create<ManagedVirtualDevice>("Google32") {
                device = "Nexus 6"
                apiLevel = 32
                systemImageSource = "google"
            }
            create<ManagedVirtualDevice>("Google27") {
                device = "Nexus 6"
                apiLevel = 27
                systemImageSource = "google"
            }
        }
    }
}