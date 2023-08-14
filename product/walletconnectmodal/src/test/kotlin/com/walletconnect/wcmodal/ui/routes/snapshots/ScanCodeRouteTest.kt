package com.walletconnect.wcmodal.ui.routes.snapshots

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.walletconnect.wcmodal.ui.routes.scan_code.ScanQRCodeRoute
import com.walletconnect.wcmodal.utils.ScreenShotTest
import org.junit.Test

class ScanCodeRouteTest : ScreenShotTest() {

    private val uri: String = "wc://47442c19ea7c6a7a836fa3e53af1ddd375438daaeea9acdbf595e989a731b73249a10a7cc0e343ca627e536609&a123ac423123eea12cbdsc876"

    private  val content: @Composable () -> Unit = {
        val navController = rememberNavController()
        ScanQRCodeRoute(navController = navController, uri = uri)
    }

    @Test
    fun `test ScanQRCodeRoute in LightMode`() = runScreenShotTest { content() }

    @Test
    fun `test ScanQRCodeRoute in DarkMode`() = runScreenShotTest(isDarkMode = true, content = content)
}
