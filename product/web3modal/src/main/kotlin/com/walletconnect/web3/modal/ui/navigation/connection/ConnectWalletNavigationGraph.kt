package com.walletconnect.web3.modal.ui.navigation.connection

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.walletconnect.web3.modal.ui.Web3ModalState
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.routes.connect.all_wallets.AllWalletsRoute
import com.walletconnect.web3.modal.ui.routes.connect.connect_wallet.ConnectWalletRoute
import com.walletconnect.web3.modal.ui.routes.connect.get_wallet.GetAWalletRoute
import com.walletconnect.web3.modal.ui.routes.connect.help.HelpRoute
import com.walletconnect.web3.modal.ui.routes.connect.scan_code.ScanQRCodeRoute

internal fun NavGraphBuilder.connectWalletNavGraph(
    navController: NavController,
    web3ModalState: Web3ModalState.Connect,
    retry: (() -> Unit) -> Unit
) {
    composable(route = Route.CONNECT_YOUR_WALLET.path) {
        ConnectWalletRoute(
            navController = navController,
            wallets = web3ModalState.wallets
        )
    }
    composable(route = Route.QR_CODE.path) {
        ScanQRCodeRoute(uri = web3ModalState.uri)
    }
    composable(route = Route.HELP.path) {
        HelpRoute(navController = navController)
    }
    composable(Route.GET_A_WALLET.path) {
        GetAWalletRoute(wallets = web3ModalState.wallets)
    }
    composable(Route.ALL_WALLETS.path) {
        AllWalletsRoute(
            navController = navController,
            wallets = web3ModalState.wallets,
        )
    }
    redirectRoute(web3ModalState.wallets, web3ModalState.uri, retry)
}
