package com.walletconnect.web3.modal.ui.components.internal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.domain.delegate.Web3ModalDelegate
import com.walletconnect.web3.modal.ui.Web3ModalState
import com.walletconnect.web3.modal.ui.Web3ModalViewModel
import com.walletconnect.web3.modal.ui.components.internal.root.Web3ModalRoot
import com.walletconnect.web3.modal.ui.navigation.Web3ModalNavGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

// That may be public in the future to allow users use our composable view
@Composable
internal fun Web3ModalComponent(
    navController: NavHostController = rememberNavController(),
    closeModal: () -> Unit
) {
    val web3ModalViewModel: Web3ModalViewModel = viewModel()
    val state by web3ModalViewModel.modalState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        Web3ModalDelegate
            .wcEventModels
            .filterIsInstance<Modal.Model.ApprovedSession>()
            .onEach { event ->
                web3ModalViewModel.saveSessionTopic(event.topic)
                closeModal()
            }
            .collect()
    }

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
                is Web3ModalState.Connect, is Web3ModalState.AccountState -> Web3ModalNavGraph(
                    navController = navController,
                    web3ModalState = state,
                    modifier = Modifier.imePadding(),
                    updateRecentWalletId = web3ModalViewModel::updateRecentWalletId,
                    retryConnection = web3ModalViewModel::retryConnection,
                    disconnect = {
                        web3ModalViewModel.disconnect(it) {
                            coroutineScope.launch(Dispatchers.Main) {
                                closeModal()
                            }
                        }
                    },
                    closeModal = closeModal,
                    changeChain = web3ModalViewModel::changeChain
                )

                Web3ModalState.Loading -> LoadingModalState()
                is Web3ModalState.Error -> ErrorModalState(retry = web3ModalViewModel::initModalState)
            }
        }
    }
}
