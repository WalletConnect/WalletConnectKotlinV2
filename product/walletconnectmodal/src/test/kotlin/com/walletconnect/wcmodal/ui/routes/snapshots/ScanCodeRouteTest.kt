package com.walletconnect.wcmodal.ui.routes.snapshots

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.walletconnect.wcmodal.ui.MainDispatcherRule
import com.walletconnect.wcmodal.ui.WalletConnectModalViewModel
import com.walletconnect.wcmodal.ui.routes.scan_code.ScanQRCodeRoute
import com.walletconnect.wcmodal.utils.ScreenShotTest
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ScanCodeRouteTest : ScreenShotTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val navController: NavController = mockk()
    private val viewModel: WalletConnectModalViewModel = mockk()

    @Before
    fun setup() {
        every { viewModel.fetchInitialWallets() } returns Unit
    }

    private  val content: @Composable () -> Unit = {
        ScanQRCodeRoute(navController = navController, viewModel = viewModel)
    }

    @Test
    fun `test ScanQRCodeRoute in LightMode`() = runScreenShotTest { content() }

    @Test
    fun `test ScanQRCodeRoute in DarkMode`() = runScreenShotTest(isDarkMode = true, content = content)
}
