package com.walletconnect.web3.modal.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.ui.Web3ModalState
import com.walletconnect.web3.modal.ui.routes.connect.all_wallets.AllWalletsRoute
import com.walletconnect.web3.modal.ui.routes.connect.connect_wallet.ConnectWalletRoute
import com.walletconnect.web3.modal.ui.routes.connect.get_wallet.GetAWalletRoute
import com.walletconnect.web3.modal.ui.routes.connect.help.HelpRoute
import com.walletconnect.web3.modal.ui.routes.connect.redirect.RedirectWalletRoute
import com.walletconnect.web3.modal.ui.routes.connect.scan_code.ScanQRCodeRoute

internal fun NavGraphBuilder.connectWalletNavGraph(
    navController: NavController,
    web3ModalState: Web3ModalState.Connect,
    updateRecentWalletId: (String) -> Unit,
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
    composable(
        route = Route.REDIRECT.path + "/" + walletIdArg + "&" + titleArg,
        arguments = listOf(navArgument(walletIdKey) { type = NavType.StringType })
    ) { backStackEntry ->
        val wallet = web3ModalState.wallets.find { it.id == backStackEntry.arguments?.getString(walletIdKey, String.Empty) }!!
        RedirectWalletRoute(wallet = wallet, uri = web3ModalState.uri, retry = retry).also { updateRecentWalletId(wallet.id) }
    }
}

// move it to files after split into multiple files
internal const val walletIdKey = "walletId"
internal const val walletIdArg = "{walletId}"
internal const val titleKey = "title"
internal const val titleArg = "{title}"
