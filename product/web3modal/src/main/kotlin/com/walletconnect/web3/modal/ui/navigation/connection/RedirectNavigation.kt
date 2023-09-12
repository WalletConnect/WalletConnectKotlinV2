package com.walletconnect.web3.modal.ui.navigation.connection

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.domain.model.Wallet
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.navigation.addTitleArg
import com.walletconnect.web3.modal.ui.routes.connect.ConnectState
import com.walletconnect.web3.modal.ui.routes.connect.redirect.RedirectWalletRoute
import timber.log.Timber

private const val WALLET_ID_KEY = "walletId"
private const val WALLET_ID_ARG = "{walletId}"

internal fun NavController.navigateToRedirect(wallet: Wallet) {
    navigate(Route.REDIRECT.path + "/${wallet.id}&${wallet.name}")
}

internal fun NavGraphBuilder.redirectRoute(
    connectState: ConnectState
) {
    composable(
        route = Route.REDIRECT.path + "/" + WALLET_ID_ARG + addTitleArg(),
        arguments = listOf(navArgument(WALLET_ID_KEY) { type = NavType.StringType })
    ) { backStackEntry ->
        val walletId = backStackEntry.arguments?.getString(WALLET_ID_KEY, String.Empty)
        val wallet = connectState.wallets.find { it.id == walletId }
        wallet?.let { RedirectWalletRoute(connectState = connectState, wallet = it) } ?: Timber.e("Invalid wallet id")
    }
}