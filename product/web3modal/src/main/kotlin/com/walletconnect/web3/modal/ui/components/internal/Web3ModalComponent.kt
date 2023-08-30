package com.walletconnect.web3.modal.ui.components.internal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.walletconnect.web3.modal.ui.Web3ModalState
import com.walletconnect.web3.modal.ui.Web3ModalViewModel
import com.walletconnect.web3.modal.ui.components.internal.root.Web3ModalRoot
import com.walletconnect.web3.modal.ui.navigation.Web3ModalNavGraph

// That may be public in the future to allow users use our composable view
@Composable
internal fun Web3ModalComponent(
    navController: NavHostController = rememberNavController(),
    closeModal: () -> Unit
) {
    val web3ModalViewModel: Web3ModalViewModel = viewModel()
    val state by web3ModalViewModel.modalState.collectAsState()

    Web3ModalRoot(
        navController = navController,
        closeModal = closeModal
    ) {
        AnimatedContent(
            targetState = state,
            contentAlignment = Alignment.BottomCenter,
            transitionSpec = {
                (fadeIn() + slideInVertically(animationSpec = tween(400),
                    initialOffsetY = { fullHeight -> fullHeight })).togetherWith(fadeOut(animationSpec = tween(200)))
            },
            label = "Root Animated content"
        ) { state ->
            when (state) {
                is Web3ModalState.Connect, Web3ModalState.AccountState -> Web3ModalNavGraph(
                    navController = navController,
                    web3ModalState = state,
                    updateRecentWalletId = web3ModalViewModel::updateRecentWalletId,
                    retryConnection = web3ModalViewModel::retryConnection,
                    closeModal = closeModal
                )
                Web3ModalState.Loading -> LoadingModalState()
                is Web3ModalState.Error -> ErrorModalState(retry = web3ModalViewModel::initModalState)
            }
        }
    }
}
