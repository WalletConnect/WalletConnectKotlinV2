@file:OptIn(ExperimentalAnimationApi::class)

package com.walletconnect.web3.modal.ui.components.internal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.delegate.Web3ModalDelegate
import com.walletconnect.web3.modal.ui.Web3ModalState
import com.walletconnect.web3.modal.ui.Web3ModalViewModel
import com.walletconnect.web3.modal.ui.components.internal.root.Web3ModalRoot
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.routes.account.AccountNavGraph
import com.walletconnect.web3.modal.ui.routes.connect.ConnectionNavGraph
import com.walletconnect.web3.modal.ui.utils.ComposableLifecycleEffect
import com.walletconnect.web3.modal.ui.utils.toComponentEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun Web3ModalComponent(
    shouldOpenChooseNetwork: Boolean,
    closeModal: () -> Unit
) {
    Web3ModalComponent(
        navController = rememberAnimatedNavController(),
        shouldOpenChooseNetwork = shouldOpenChooseNetwork,
        closeModal = closeModal
    )
}

@Composable
internal fun Web3ModalComponent(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberAnimatedNavController(),
    shouldOpenChooseNetwork: Boolean,
    closeModal: () -> Unit
) {
    val web3ModalViewModel: Web3ModalViewModel = viewModel()
    val state by web3ModalViewModel.modalState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        Web3ModalDelegate
            .wcEventModels
            .onEach { event ->
                when (event) {
                    is Modal.Model.SIWEAuthenticateResponse.Result, is Modal.Model.SessionAuthenticateResponse.Result -> closeModal()
                    is Modal.Model.ApprovedSession -> {
                        if (Web3Modal.authPayloadParams != null) {
                            navController.navigate(Route.SIWE_FALLBACK.path)
                        } else {
                            closeModal()
                        }
                    }
                    is Modal.Model.DeletedSession.Success -> closeModal()

                    else -> Unit
                }
            }
            .collect()
    }

    ComposableLifecycleEffect(
        onEvent = { _, event ->
            coroutineScope.launch {
                event.toComponentEvent(onClosed = {
                    if (navController.currentDestination?.route == Route.SIWE_FALLBACK.path && web3ModalViewModel.shouldDisconnect) {
                        web3ModalViewModel.disconnect()
                    }
                })
            }
        }
    )

    Web3ModalRoot(
        modifier = modifier,
        navController = navController,
        closeModal = closeModal
    ) {
        AnimatedContent(
            targetState = state,
            contentAlignment = Alignment.BottomCenter,
            transitionSpec = {
                (fadeIn() + slideInVertically(animationSpec = tween(500),
                    initialOffsetY = { fullHeight -> fullHeight })).togetherWith(fadeOut(animationSpec = tween(500)))
            },
            label = "Root Animated content"
        ) { state ->
            when (state) {
                is Web3ModalState.Connect -> ConnectionNavGraph(
                    navController = navController,
                    closeModal = closeModal,
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
