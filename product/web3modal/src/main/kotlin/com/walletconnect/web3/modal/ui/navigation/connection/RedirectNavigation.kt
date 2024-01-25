@file:OptIn(ExperimentalAnimationApi::class)

package com.walletconnect.web3.modal.ui.navigation.connection

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.composable
import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.navigation.addTitleArg
import com.walletconnect.web3.modal.ui.routes.connect.ConnectViewModel
import com.walletconnect.web3.modal.ui.routes.connect.redirect.RedirectWalletRoute
import timber.log.Timber

private const val WALLET_ID_KEY = "walletId"
private const val WALLET_ID_ARG = "{walletId}"

internal fun Wallet.toRedirectPath() = Route.REDIRECT.path + "/${id}&${name}"

internal fun NavGraphBuilder.redirectRoute(
    connectViewModel: ConnectViewModel
) {
    composable(
        route = Route.REDIRECT.path + "/" + WALLET_ID_ARG + addTitleArg(),
        arguments = listOf(navArgument(WALLET_ID_KEY) { type = NavType.StringType })
    ) { backStackEntry ->
        val walletId = backStackEntry.arguments?.getString(WALLET_ID_KEY, String.Empty)
        val wallet = connectViewModel.getWallet(walletId)
        wallet?.let { RedirectWalletRoute(connectState = connectViewModel, wallet = it) } ?: Timber.e("Invalid wallet id")
    }
}
