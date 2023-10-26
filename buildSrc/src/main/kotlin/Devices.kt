import com.android.build.api.dsl.ManagedVirtualDevice
import com.android.build.api.dsl.TestOptions
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke

fun TestOptions.registerManagedDevices() {
    managedDevices {
        devices {
            val _deviceName = "Pixel 5"
            val _apiLevel = 32
            val _systemImageSource = "google"
            val taskSuffix = "_" + _deviceName.replace(" ", "_")
            create<ManagedVirtualDevice>("${_systemImageSource}${_apiLevel}${taskSuffix}") {
                device = _deviceName
                apiLevel = _apiLevel
                systemImageSource = _systemImageSource
            }
        }
    }
}