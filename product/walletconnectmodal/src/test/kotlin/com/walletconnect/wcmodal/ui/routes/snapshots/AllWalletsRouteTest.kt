package com.walletconnect.wcmodal.ui.routes.snapshots

import androidx.navigation.NavController
import com.walletconnect.wcmodal.ui.MainDispatcherRule
import com.walletconnect.wcmodal.ui.routes.all_wallets.AllWalletsRoute
import com.walletconnect.wcmodal.utils.ScreenShotTest
import com.walletconnect.wcmodal.utils.wallets
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class AllWalletsRouteTest: ScreenShotTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val navController: NavController = mockk()

    @Test
    fun `test AllWalletsRoute in LightMode with empty list of wallets`() = runScreenShotTest {
        AllWalletsRoute(navController = navController, wallets = emptyList())
    }

    @Test
    fun `test AllWalletsRoute in DarkMode with empty list of wallets`() = runScreenShotTest(isDarkMode = true) {
        AllWalletsRoute(navController = navController, wallets = emptyList())
    }

    @Test
    fun `test AllWalletsRoute in LightMode with 3 wallets`() = runScreenShotTest {
        AllWalletsRoute(navController = navController, wallets = wallets.take(3))
    }

    @Test
    fun `test AllWalletsRoute in DarkMode with 3 wallets`() = runScreenShotTest(isDarkMode = true) {
        AllWalletsRoute(navController = navController, wallets = wallets.take(3))
    }

    @Test
    fun `test AllWalletsRoute in LightMode with 10 wallets`() = runScreenShotTest {
        AllWalletsRoute(navController = navController, wallets = wallets.take(10))
    }

    @Test
    fun `test AllWalletsRoute in DarkMode with 10 wallets`() = runScreenShotTest(isDarkMode = true) {
        AllWalletsRoute(navController = navController, wallets = wallets.take(10))
    }

    @Test
    fun `test AllWalletsRoute in LightMode whole wallets list`() = runScreenShotTest {
        AllWalletsRoute(navController = navController, wallets = wallets)
    }

    @Test
    fun `test AllWalletsRoute in DarkMode with whole wallets list`() = runScreenShotTest(isDarkMode = true) {
        AllWalletsRoute(navController = navController, wallets = wallets)
    }
}
