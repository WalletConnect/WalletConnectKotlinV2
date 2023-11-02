package com.walletconnect.web3.modal.ui.routes.connect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.walletconnect.web3.modal.ui.components.internal.snackbar.LocalSnackBarHandler
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.navigation.connection.redirectRoute
import com.walletconnect.web3.modal.ui.routes.connect.choose_network.ChooseNetworkRoute
import com.walletconnect.web3.modal.ui.routes.common.WhatIsNetworkRoute
import com.walletconnect.web3.modal.ui.routes.connect.all_wallets.AllWalletsRoute
import com.walletconnect.web3.modal.ui.routes.connect.connect_wallet.ConnectWalletRoute
import com.walletconnect.web3.modal.ui.routes.connect.get_wallet.GetAWalletRoute
import com.walletconnect.web3.modal.ui.routes.connect.what_is_wallet.WhatIsWallet
import com.walletconnect.web3.modal.ui.routes.connect.scan_code.ScanQRCodeRoute
import com.walletconnect.web3.modal.ui.routes.connect.what_is_wallet.WhatIsWalletOption
import com.walletconnect.web3.modal.ui.utils.AnimatedNavGraph

@Composable
internal fun ConnectionNavGraph(
    navController: NavHostController,
    shouldOpenChooseNetwork: Boolean
) {
    val snackBar = LocalSnackBarHandler.current
    val connectState = rememberConnectState(
        coroutineScope = rememberCoroutineScope(),
        navController = navController
    ) { message -> snackBar.showErrorSnack(message ?: "Something went wrong") }
    val startDestination = if (shouldOpenChooseNetwork) { Route.CHOOSE_NETWORK.path } else { Route.CONNECT_YOUR_WALLET.path }
    AnimatedNavGraph(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Route.CONNECT_YOUR_WALLET.path) {
            ConnectWalletRoute(connectState = connectState)
        }
        composable(route = Route.QR_CODE.path) {
            ScanQRCodeRoute(connectState = connectState)
        }
        composable(route = Route.WHAT_IS_WALLET.path) {
            WhatIsWallet(
                navController = navController,
                option = WhatIsWalletOption.GET_WALLET
            )
        }
        composable(Route.GET_A_WALLET.path) {
            GetAWalletRoute(wallets = connectState.getNotInstalledWallets() )
        }
        composable(Route.ALL_WALLETS.path) {
            AllWalletsRoute(connectState = connectState)
        }
        redirectRoute(connectState)
        composable(Route.CHOOSE_NETWORK.path) {
            ChooseNetworkRoute(connectState = connectState)
        }
        composable(Route.WHAT_IS_NETWORK.path) {
            WhatIsNetworkRoute()
        }
    }
}
