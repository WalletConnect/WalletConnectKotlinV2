package com.walletconnect.web3.modal.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.HtmlReportWriter
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.SnapshotVerifier
import app.cash.paparazzi.detectEnvironment
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.KeyboardState
import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.web3.modal.ui.components.internal.root.Web3ModalRoot
import com.walletconnect.web3.modal.ui.components.internal.root.rememberWeb3ModalRootState
import com.walletconnect.web3.modal.ui.components.internal.snackbar.rememberSnackBarState
import com.walletconnect.web3.modal.ui.theme.ProvideWeb3ModalThemeComposition
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import org.junit.Rule
import org.koin.dsl.module
import java.io.File

internal abstract class ScreenShotTest(
    testDirectoryName: String
) {

    private val isVerifying: Boolean = System.getProperty("paparazzi.test.verify")?.toBoolean() == true

    // Place images in flavor specific sub-folders.
    val snapshotHandler = if (isVerifying) {
        SnapshotVerifier(
            maxPercentDifference = 0.01,
            rootDirectory = File("src/test/snapshots/$testDirectoryName")
        )
    } else {
        HtmlReportWriter(
            snapshotRootDirectory = File("src/test/snapshots/$testDirectoryName")
        )
    }

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_2_XL,
        renderingMode = SessionParams.RenderingMode.SHRINK,
        snapshotHandler = snapshotHandler,
        environment = detectEnvironment().copy()
    )

    fun runRouteScreenShotTest(
        title: String?,
        nightMode: NightMode = NightMode.NOTNIGHT,
        orientation: ScreenOrientation = ScreenOrientation.PORTRAIT,
        keyboardState: KeyboardState = KeyboardState.HIDDEN,
        content: @Composable () -> Unit
    ) = with(paparazzi) {
        paparazziKoinDefinitions()
        val screenHeight: Int
        val screenWidth: Int
        if (orientation == ScreenOrientation.PORTRAIT) {
            screenHeight = 1280
            screenWidth = 768
        } else {
            screenWidth = 1280
            screenHeight = 768
        }
        unsafeUpdateConfig(
            DeviceConfig(nightMode = nightMode, orientation = orientation, keyboardState = keyboardState, screenHeight = screenHeight, screenWidth = screenWidth)
        )
        snapshot {
            val scope = rememberCoroutineScope()
            val rootState = rememberWeb3ModalRootState(coroutineScope = scope, navController = rememberNavController())
            val snackBarState = rememberSnackBarState(coroutineScope = scope)
            ProvideWeb3ModalThemeComposition {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Web3ModalRoot(
                        rootState = rootState,
                        snackBarState = snackBarState,
                        closeModal = {},
                        title = title
                    ) {
                        content()
                    }
                }
            }
        }
    }

    fun runComponentScreenShotTest(
        nightMode: NightMode = NightMode.NOTNIGHT,
        orientation: ScreenOrientation = ScreenOrientation.PORTRAIT,
        keyboardState: KeyboardState = KeyboardState.HIDDEN,
        content: @Composable ColumnScope.() -> Unit
    ) = with(paparazzi) {
        paparazziKoinDefinitions()
        unsafeUpdateConfig(
            DeviceConfig(nightMode = nightMode, orientation = orientation, keyboardState = keyboardState)
        )
        snapshot {
            ProvideWeb3ModalThemeComposition {
                Box(modifier = Modifier.background(Web3ModalTheme.colors.background.color100)) {
                    Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        content()
                    }
                }
            }
        }
    }

    private fun paparazziKoinDefinitions() {
        val modules = listOf(
            module { single { ProjectId("fakeId") } }
        )
        wcKoinApp.koin.loadModules(modules = modules)
    }
}
