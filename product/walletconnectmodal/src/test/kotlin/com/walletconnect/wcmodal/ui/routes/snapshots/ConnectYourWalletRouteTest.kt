package com.walletconnect.wcmodal.ui.routes.snapshots

import androidx.navigation.NavController
import com.walletconnect.wcmodal.ui.MainDispatcherRule
import com.walletconnect.wcmodal.ui.routes.connect_wallet.ConnectYourWalletRoute
import com.walletconnect.wcmodal.utils.ScreenShotTest
import com.walletconnect.wcmodal.utils.wallets
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class ConnectYourWalletRouteTest: ScreenShotTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val navController: NavController = mockk()

    @Test
    fun `test ConnectYourWalletRoute in LightMode with empty list of wallets`() = runScreenShotTest {
        ConnectYourWalletRoute(navController, emptyList())
    }

    @Test
    fun `test ConnectYourWalletRoute in DarkMode with empty list of wallets`() = runScreenShotTest(isDarkMode = true) {
        ConnectYourWalletRoute(navController, emptyList())
    }

    @Test
    fun `test ConnectYourWalletRoute in LightMode with 3 wallets`() = runScreenShotTest {
        ConnectYourWalletRoute(navController = navController, wallets = wallets.take(3))
    }

    @Test
    fun `test ConnectYourWalletRoute in DarkMode with 3 wallets`() = runScreenShotTest(isDarkMode = true) {
        ConnectYourWalletRoute(navController = navController, wallets = wallets.take(3))
    }

    @Test
    fun `test ConnectYourWalletRoute in LightMode with 11 wallets`() = runScreenShotTest {
        ConnectYourWalletRoute(navController = navController, wallets = wallets.take(11))
    }

    @Test
    fun `test ConnectYourWalletRoute in DarkMode with 11 wallets`() = runScreenShotTest(isDarkMode = true) {
        ConnectYourWalletRoute(navController = navController, wallets = wallets.take(11))
    }
}
