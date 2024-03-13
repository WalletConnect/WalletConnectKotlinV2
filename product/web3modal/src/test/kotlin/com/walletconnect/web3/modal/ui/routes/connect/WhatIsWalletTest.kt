package com.walletconnect.web3.modal.ui.routes.connect

import androidx.navigation.NavController
import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.routes.connect.what_is_wallet.WhatIsWallet
import com.walletconnect.web3.modal.utils.MainDispatcherRule
import com.walletconnect.web3.modal.utils.ScreenShotTest
import io.mockk.mockk
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@Ignore("This test is not working on CI for Sonar only")
internal class WhatIsWalletTest: ScreenShotTest("connect/${Route.WHAT_IS_WALLET.path}") {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val navController: NavController = mockk()

    @Test
    fun `test WhatIsWallet in LightMode`() = runRouteScreenShotTest(
        title = Route.WHAT_IS_WALLET.title
    ) {
        WhatIsWallet(navController)
    }

    @Test
    fun `test WhatIsWallet in DarkMode`() = runRouteScreenShotTest(
        title = Route.WHAT_IS_WALLET.title,
        nightMode = NightMode.NIGHT
    ) {
        WhatIsWallet(navController)
    }

    @Test
    fun `test WhatIsWallet in Landscape`() = runRouteScreenShotTest(
        title = Route.WHAT_IS_WALLET.title,
        orientation = ScreenOrientation.LANDSCAPE
    ) {
        WhatIsWallet(navController)
    }
}
