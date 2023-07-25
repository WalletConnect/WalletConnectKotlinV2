import com.android.build.api.dsl.ManagedVirtualDevice
import com.android.build.api.dsl.TestOptions
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke


// Device profiles taken from `$avdmanager list device` and tested locally for validity on 25/07/2023
val devicesNames = listOf(
    "Pixel 5", "Galaxy Nexus", "Nexus 10", "Nexus 4", "Nexus 5", "Nexus 5X", "Nexus 6", "Nexus 6P",
    "Nexus 7", "Nexus 9", "Nexus One", "Nexus S", "Pixel", "Pixel 2", "Pixel 2 XL", "Pixel 3", "Pixel 3 XL",
    "Pixel 3a", "Pixel 3a XL", "Pixel 4", "Pixel 4 XL", "Pixel 4a", "Pixel C", "Pixel XL"
)

fun TestOptions.registerManagedDevices() {
    val whichDeviceProfile = (System.getenv("GITHUB_RUN_NUMBER").toIntOrNull() ?: 0).mod(devicesNames.size)
    managedDevices {
        devices {
            devicesNames[whichDeviceProfile].let { deviceName ->
                val taskSuffix = deviceName.replace(" ", "_")
                create<ManagedVirtualDevice>("Google32_${taskSuffix}") {
                    device = deviceName
                    apiLevel = 32
                    systemImageSource = "google-atd"
                }
                create<ManagedVirtualDevice>("Google27_${taskSuffix}") {
                    device = deviceName
                    apiLevel = 27
                    systemImageSource = "google-atd"
                }
            }
        }
    }
}