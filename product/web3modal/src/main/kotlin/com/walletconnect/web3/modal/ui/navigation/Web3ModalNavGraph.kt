package com.walletconnect.web3.modal.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.walletconnect.web3.modal.ui.Web3ModalState
import com.walletconnect.web3.modal.ui.toStartingPath

@Composable
internal fun Web3ModalNavGraph(
    navController: NavHostController,
    web3ModalState: Web3ModalState,
    modifier: Modifier = Modifier,
    updateRecentWalletId: (String) -> Unit,
    retryConnection: (() -> Unit) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = web3ModalState.toStartingPath(),
        modifier = modifier,
        enterTransition = { fadeIn(tween()) },
        popExitTransition = { fadeOut(tween()) },
        exitTransition = { fadeOut(tween()) },
        popEnterTransition = { fadeIn(tween()) }
    ) {
        when (web3ModalState) {
            is Web3ModalState.Connect -> connectWalletNavGraph(navController, web3ModalState, updateRecentWalletId, retryConnection)
            Web3ModalState.SessionState -> sessionModalGraph(
                navController,
                Web3ModalState.SessionState
            )
            else -> {}
        }
    }
}
