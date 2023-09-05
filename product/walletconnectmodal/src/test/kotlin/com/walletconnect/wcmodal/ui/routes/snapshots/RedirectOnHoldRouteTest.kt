package com.walletconnect.wcmodal.ui.routes.snapshots

import androidx.navigation.NavController
import com.walletconnect.wcmodal.ui.MainDispatcherRule
import com.walletconnect.wcmodal.ui.routes.on_hold.RedirectOnHoldScreen
import com.walletconnect.wcmodal.utils.ScreenShotTest
import com.walletconnect.wcmodal.utils.metaMask
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class RedirectOnHoldRouteTest: ScreenShotTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val uri: String = "wc://47442c19ea7c6a7a836fa3e53af1ddd375438daaeea9acdbf595e989a731b73249a10a7cc0e343ca627e536609&a123ac423123eea12cbdsc876"
    private val navController: NavController = mockk()

    @Test
    fun `test RedirectOnHoldRoute in LightMode`() = runScreenShotTest {
        RedirectOnHoldScreen(navController = navController, uri = uri, wallet = metaMask, retry = {})
    }

    @Test
    fun `test RedirectOnHoldRoute in DarkMode`() = runScreenShotTest(isDarkMode = true) {
        RedirectOnHoldScreen(navController = navController, uri = uri, wallet = metaMask, retry = {})
    }
}