package com.walletconnect.web3.modal.ui.components.internal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.domain.delegate.Web3ModalDelegate
import com.walletconnect.web3.modal.ui.Web3ModalState
import com.walletconnect.web3.modal.ui.Web3ModalViewModel
import com.walletconnect.web3.modal.ui.components.internal.root.Web3ModalRoot
import com.walletconnect.web3.modal.ui.routes.account.AccountNavGraph
import com.walletconnect.web3.modal.ui.routes.connect.ConnectionNavGraph
import com.walletconnect.web3.modal.ui.utils.ComposableLifecycleEffect
import com.walletconnect.web3.modal.ui.utils.toComponentEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun Web3ModalComponent(
    navController: NavHostController = rememberNavController(),
    shouldOpenChooseNetwork: Boolean,
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

    ComposableLifecycleEffect(
        onEvent = { _, event ->
            coroutineScope.launch { event.toComponentEvent() }
        }
    )

    Web3ModalRoot(
        navController = navController,
        closeModal = closeModal
    ) {
        AnimatedContent(
            targetState = state,
            contentAlignment = Alignment.BottomCenter,
            transitionSpec = {
                (fadeIn() + slideInVertically(animationSpec = tween(400),
                    initialOffsetY = { fullHeight -> fullHeight })).togetherWith(fadeOut(animationSpec = tween(400)))
            },
            label = "Root Animated content"
        ) { state ->
            when (state) {
                is Web3ModalState.Connect -> ConnectionNavGraph(
                    navController = navController,
                    shouldOpenChooseNetwork = shouldOpenChooseNetwork
                )
                is Web3ModalState.AccountState -> AccountNavGraph(
                    navController = navController,
                    closeModal = closeModal,
                    shouldOpenChangeNetwork = shouldOpenChooseNetwork
                )
                Web3ModalState.Loading -> LoadingModalState()
                is Web3ModalState.Error -> ErrorModalState(retry = web3ModalViewModel::initModalState)
            }
        }
    }
}
