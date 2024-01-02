package com.walletconnect.web3.modal.ui.routes.connect

import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.previews.testWallets
import com.walletconnect.web3.modal.ui.routes.connect.get_wallet.GetAWalletRoute
import com.walletconnect.web3.modal.utils.MainDispatcherRule
import com.walletconnect.web3.modal.utils.ScreenShotTest
import org.junit.Rule
import org.junit.Test

internal class GetAWalletRouteTest: ScreenShotTest("connect/${Route.GET_A_WALLET.path}") {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test GetAWalletRoute in LightMode`() = runRouteScreenShotTest(
        title = Route.GET_A_WALLET.title
    ) {
        GetAWalletRoute(testWallets)
    }

    @Test
    fun `test GetAWalletRoute in DarkMode`() = runRouteScreenShotTest(
        title = Route.GET_A_WALLET.title,
        nightMode = NightMode.NIGHT
    ) {
        GetAWalletRoute(testWallets)
    }

    @Test
    fun `test GetAWalletRoute in Landscape`() = runRouteScreenShotTest(
        title = Route.GET_A_WALLET.title,
        orientation = ScreenOrientation.LANDSCAPE
    ) {
        GetAWalletRoute(testWallets)
    }
}
