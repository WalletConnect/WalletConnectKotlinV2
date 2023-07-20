@file:OptIn(ExperimentalAnimationApi::class)

package com.walletconnect.wcmodal.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.walletconnect.wcmodal.ui.WalletConnectModalState
import com.walletconnect.wcmodal.ui.routes.all_wallets.AllWalletsRoute
import com.walletconnect.wcmodal.ui.routes.connect_wallet.ConnectYourWalletRoute
import com.walletconnect.wcmodal.ui.routes.get_wallet.GetAWalletRoute
import com.walletconnect.wcmodal.ui.routes.help.HelpRoute
import com.walletconnect.wcmodal.ui.routes.scan_code.ScanQRCodeRoute

@Composable
internal fun ModalNavGraph(
    navController: NavHostController,
    state: WalletConnectModalState,
    modifier: Modifier = Modifier
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = Route.ConnectYourWallet.path,
        modifier = modifier
    ) {
        animatedComposable(route = Route.ConnectYourWallet.path) {
            ConnectYourWalletRoute(navController = navController, uri = state.uri, wallets = state.wallets)
        }
        animatedComposable(route = Route.ScanQRCode.path) {
            ScanQRCodeRoute(navController = navController, uri = state.uri)
        }
        animatedComposable(route = Route.Help.path) {
            HelpRoute(navController = navController)
        }
        animatedComposable(route = Route.AllWallets.path) {
            AllWalletsRoute(navController = navController, uri = state.uri, wallets = state.wallets)
        }
        animatedComposable(route = Route.GetAWallet.path) {
            GetAWalletRoute(navController = navController, wallets = state.wallets)
        }
    }
}

internal fun NavGraphBuilder.animatedComposable(
    route: String,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        content = { content(it) }
    )
}