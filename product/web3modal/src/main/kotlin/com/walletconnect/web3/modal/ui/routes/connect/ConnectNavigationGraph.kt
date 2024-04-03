package com.walletconnect.web3.modal.ui.routes.connect

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.walletconnect.web3.modal.ui.navigation.ConsumeNavigationEventsEffect
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.navigation.connection.redirectRoute
import com.walletconnect.web3.modal.ui.routes.common.WhatIsNetworkRoute
import com.walletconnect.web3.modal.ui.routes.connect.all_wallets.AllWalletsRoute
import com.walletconnect.web3.modal.ui.routes.connect.choose_network.ChooseNetworkRoute
import com.walletconnect.web3.modal.ui.routes.connect.connect_wallet.ConnectWalletRoute
import com.walletconnect.web3.modal.ui.routes.connect.get_wallet.GetAWalletRoute
import com.walletconnect.web3.modal.ui.routes.connect.scan_code.ScanQRCodeRoute
import com.walletconnect.web3.modal.ui.routes.connect.what_is_wallet.WhatIsWallet
import com.walletconnect.web3.modal.ui.utils.AnimatedNavGraph
import com.walletconnect.web3.modal.ui.utils.animatedComposable

@Composable
internal fun ConnectionNavGraph(
    navController: NavHostController,
    shouldOpenChooseNetwork: Boolean
) {
    val connectViewModel = viewModel<ConnectViewModel>()
    val startDestination = if (shouldOpenChooseNetwork) {
        Route.CHOOSE_NETWORK.path
    } else {
        Route.CONNECT_YOUR_WALLET.path
    }

    ConsumeNavigationEventsEffect(
        navController = navController,
        navigator = connectViewModel
    )

    AnimatedNavGraph(
        navController = navController,
        startDestination = startDestination
    ) {
        animatedComposable(route = Route.CONNECT_YOUR_WALLET.path) {
            ConnectWalletRoute(connectViewModel = connectViewModel)
        }
        animatedComposable(route = Route.QR_CODE.path) {
            ScanQRCodeRoute(connectViewModel = connectViewModel)
        }
        animatedComposable(route = Route.WHAT_IS_WALLET.path) {
            WhatIsWallet(navController = navController)
        }
        animatedComposable(route = Route.REGISTER_DEVICE.path) {

        }
        animatedComposable(route = Route.CONFIRM_EMAIL.path) {

        }
        animatedComposable(Route.GET_A_WALLET.path) {
            GetAWalletRoute(wallets = connectViewModel.getNotInstalledWallets())
        }
        animatedComposable(Route.ALL_WALLETS.path) {
            AllWalletsRoute(connectViewModel = connectViewModel)
        }
        redirectRoute(connectViewModel)
        animatedComposable(Route.CHOOSE_NETWORK.path) {
            ChooseNetworkRoute(connectViewModel = connectViewModel)
        }
        animatedComposable(Route.WHAT_IS_NETWORK.path) {
            WhatIsNetworkRoute()
        }
    }
}
