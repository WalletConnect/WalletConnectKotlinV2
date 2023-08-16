package com.walletconnect.web3.modal.ui.components.internal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.walletconnect.modal.ui.components.common.VerticalSpacer
import com.walletconnect.web3.modal.ui.Web3ModalState
import com.walletconnect.web3.modal.ui.Web3ModalViewModel
import com.walletconnect.web3.modal.ui.components.internal.commons.LoadingSpinner
import com.walletconnect.web3.modal.ui.components.internal.commons.RoundedMainButton
import com.walletconnect.web3.modal.ui.components.internal.root.Web3ModalRoot
import com.walletconnect.web3.modal.ui.navigation.Web3ModalNavGraph
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

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
            label = "Root content"
        ) { state ->
            when (state) {
                is Web3ModalState.Connect, Web3ModalState.SessionState -> Web3ModalNavGraph(
                    navController = navController,
                    web3ModalState = state,
                )
                Web3ModalState.Loading -> LoadingModalState()
                is Web3ModalState.Error -> ErrorModalState(closeModal = closeModal)
            }
        }
    }
}

@Composable
private fun LoadingModalState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center,
    ) {
        LoadingSpinner()
    }
}

@Composable
private fun ErrorModalState(closeModal: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Something went wrong", style = Web3ModalTheme.typo.paragraph500.copy(color = Web3ModalTheme.colors.foreground.color100))
        VerticalSpacer(height = 10.dp)
        RoundedMainButton(
            text = "Close",
            onClick = { closeModal() },
        )
    }
}
