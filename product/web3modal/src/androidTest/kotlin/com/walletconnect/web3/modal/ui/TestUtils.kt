package com.walletconnect.web3.modal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.navigation.addTitleArg

@Composable
fun rememberTestNavController(): TestNavHostController {
    val context = LocalContext.current
    return remember {
        TestNavHostController(context).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            graph = createTestGraph()
        }
    }
}

private fun NavController.createTestGraph() = createGraph(Route.CONNECT_YOUR_WALLET.path) {
    composable(Route.CONNECT_YOUR_WALLET.path) { }
    composable(Route.QR_CODE.path) { }
    composable(Route.WHAT_IS_WALLET.path) { }
    composable(Route.REDIRECT.path + addTitleArg()) {}
    composable("A") { }
}
