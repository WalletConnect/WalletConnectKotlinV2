@file:OptIn(ExperimentalAnimationApi::class)

package com.walletconnect.modal.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.walletconnect.modal.ui.components.ModalRoot
import com.walletconnect.modal.ui.navigation.ModalNavGraph
import com.walletconnect.modal.ui.theme.ModalTheme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
internal fun WalletConnectModal(
    navController: NavController
) {
    WalletConnectModalComponent(
        closeModal = { navController.popBackStack() }
    )
}

@ExperimentalAnimationApi
@Composable
internal fun WalletConnectModalComponent(
    navController: NavHostController = rememberAnimatedNavController(),
    closeModal: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: WalletConnectModalViewModel = viewModel()
    val state by viewModel.modalState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel
            .modalEvents
            .onEach { event -> event.handleEvent(context, closeModal) }
            .collect()
    }

    ModalRoot(
        navController = navController,
        closeModal = closeModal,
    ) {
        AnimatedContent(
            targetState = state,
            contentAlignment = Alignment.BottomCenter,
            transitionSpec = {
                fadeIn() + slideInVertically(animationSpec = tween(400),
                    initialOffsetY = { fullHeight -> fullHeight }) with
                        fadeOut(animationSpec = tween(200))
            }
        ) { state ->
            if (state == null) {
                LoadingModalState()
            } else {
                ModalNavGraph(
                    navController = navController,
                    state = state
                )
            }

        }
    }
}


@Composable
private fun LoadingModalState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        CircularProgressIndicator(color = ModalTheme.colors.main, modifier = Modifier.align(Alignment.Center))
    }
}

private fun WalletConnectModalEvents.handleEvent(
    context: Context,
    closeModal: () -> Unit
) {
    when (this) {
        WalletConnectModalEvents.InvalidState -> closeModal()
        WalletConnectModalEvents.NoAction -> Unit
        WalletConnectModalEvents.SessionApproved -> {
            closeModal()
            Toast.makeText(context, "Session was approved", Toast.LENGTH_SHORT).show()
        }
        WalletConnectModalEvents.SessionRejected -> {
            Toast.makeText(context, "Session was rejected", Toast.LENGTH_SHORT).show()
        }
    }
}
