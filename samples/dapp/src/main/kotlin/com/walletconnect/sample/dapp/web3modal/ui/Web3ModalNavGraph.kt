@file:OptIn(ExperimentalAnimationApi::class)

package com.walletconnect.sample.dapp.web3modal.ui

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
import com.walletconnect.sample.dapp.web3modal.ui.routes.connect_wallet.ConnectYourWalletRoute
import com.walletconnect.sample.dapp.web3modal.ui.routes.help.HelpRoute
import com.walletconnect.sample.dapp.web3modal.ui.routes.scan_code.ScanQRCodeRoute

@Composable
fun Web3ModalNavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        animatedComposable(Route.ConnectYourWallet.path) {
            ConnectYourWalletRoute(navController = navController)
        }
        animatedComposable(Route.ScanQRCode.path) {
            ScanQRCodeRoute(navController = navController)
        }
        animatedComposable(Route.Help.path) {
            HelpRoute(navController = navController)
        }
    }
}


private fun NavGraphBuilder.animatedComposable(route: String, content: @Composable (NavBackStackEntry) -> Unit) {
    composable(
        route = route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        content = { content(it) }
    )
}