@file:OptIn(ExperimentalAnimationApi::class, ExperimentalAnimationApi::class)

package com.walletconnect.web3.modal.ui.navigation

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
import com.walletconnect.web3.modal.ui.Web3ModalState
import com.walletconnect.web3.modal.ui.toStartingPath

@Composable
internal fun Web3ModalNavGraph(
    navController: NavHostController,
    web3ModalState: Web3ModalState,
    modifier: Modifier = Modifier,
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = web3ModalState.toStartingPath(),
        modifier = modifier,
    ) {
        when (web3ModalState) {
            is Web3ModalState.Connect -> connectWalletNavGraph(navController, web3ModalState)
            Web3ModalState.SessionState -> sessionModalGraph(
                navController,
                Web3ModalState.SessionState
            )
            else -> {}
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
