package com.walletconnect.wcmodal.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.NightMode
import com.walletconnect.wcmodal.ui.theme.ModalTheme
import com.walletconnect.wcmodal.ui.theme.ProvideModalThemeComposition
import org.junit.Rule

abstract class ScreenShotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_2_XL,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    fun runScreenShotTest(
        isDarkMode: Boolean = false,
        content: @Composable () -> Unit
    ) {
        val mode = if (isDarkMode) NightMode.NIGHT else NightMode.NOTNIGHT
        paparazzi.unsafeUpdateConfig(DeviceConfig(nightMode = mode))
        paparazzi.snapshot {
            ProvideModalThemeComposition {
                Box(modifier = Modifier.background(ModalTheme.colors.background)) {
                    content()
                }
            }
        }
    }
}
