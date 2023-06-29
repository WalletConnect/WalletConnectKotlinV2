package com.walletconnect.web3.modal.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.walletconnect.web3.modal.ui.Web3ModalState
import com.walletconnect.web3.modal.ui.routes.connect.all_wallets.AllWalletsRoute
import com.walletconnect.web3.modal.ui.routes.connect.connect_wallet.ConnectYourWalletRoute
import com.walletconnect.web3.modal.ui.routes.connect.get_wallet.GetAWalletRoute
import com.walletconnect.web3.modal.ui.routes.connect.help.HelpRoute
import com.walletconnect.web3.modal.ui.routes.connect.scan_code.ScanQRCodeRoute

internal fun NavGraphBuilder.connectWalletNavGraph(
    navController: NavController,
    web3ModalState: Web3ModalState.ConnectState
) {
    animatedComposable(route = Route.ConnectYourWallet.path) {
        ConnectYourWalletRoute(
            navController = navController,
            uri = web3ModalState.uri,
            wallets = web3ModalState.wallets
        )
    }
    animatedComposable(route = Route.ScanQRCode.path) {
        ScanQRCodeRoute(
            navController = navController,
            uri = web3ModalState.uri
        )
    }
    animatedComposable(route = Route.Help.path) {
        HelpRoute(navController = navController)
    }
    animatedComposable(Route.GetAWallet.path) {
        GetAWalletRoute(
            navController = navController,
            wallets = web3ModalState.wallets
        )
    }
    animatedComposable(Route.AllWallets.path) {
        AllWalletsRoute(
            navController = navController,
            wallets = web3ModalState.wallets,
            uri = web3ModalState.uri
        )
    }
}
