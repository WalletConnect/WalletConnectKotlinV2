package com.walletconnect.web3.modal.ui.routes.connect

import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.routes.connect.scan_code.ScanQRCodeRoute
import com.walletconnect.web3.modal.utils.MainDispatcherRule
import com.walletconnect.web3.modal.utils.ScreenShotTest
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@Ignore("This test is not working on CI for Sonar only")
internal class ScanCodeRouteTest: ScreenShotTest("connect/${Route.QR_CODE.path}") {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel: ConnectViewModel = mockk()

    @Before
    fun setup() {
        every { viewModel.uri } returns "wc:7f6e504bfad60b485450578e05678ed3e8e8c4751d3c6160be17160d63ec90f9@2?relay-protocol=irn&symKey=587d5484ce2a2a6ee3ba1962fdd7e8588e06200c46823bd18fbd67def96ad303"
    }

    @Test
    fun `test ScanCodeRoute in LightMode`() = runRouteScreenShotTest(
        title = Route.QR_CODE.title
    ) {
        ScanQRCodeRoute(connectViewModel = viewModel)
    }

    @Test
    fun `test ScanCodeRoute in DarkMode`() = runRouteScreenShotTest(
        title = Route.QR_CODE.title,
        nightMode = NightMode.NIGHT
    ) {
        ScanQRCodeRoute(connectViewModel = viewModel)
    }

    @Test
    fun `test ScanCodeRoute in Landscape`() = runRouteScreenShotTest(
        title = Route.QR_CODE.title,
        orientation = ScreenOrientation.LANDSCAPE
    ) {
        ScanQRCodeRoute(connectViewModel = viewModel)
    }
}
