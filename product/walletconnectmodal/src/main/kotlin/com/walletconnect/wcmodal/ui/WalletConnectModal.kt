@file:OptIn(ExperimentalAnimationApi::class)

package com.walletconnect.wcmodal.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.walletconnect.modal.ui.components.common.VerticalSpacer
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.domain.WalletConnectModalDelegate
import com.walletconnect.wcmodal.ui.components.ModalRoot
import com.walletconnect.wcmodal.ui.components.RoundedMainButton
import com.walletconnect.wcmodal.ui.navigation.ModalNavGraph
import com.walletconnect.wcmodal.ui.theme.ModalTheme
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
    navController: NavHostController = rememberNavController(),
    closeModal: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: WalletConnectModalViewModel = viewModel()
    val state by viewModel.modalState.collectAsState()

    LaunchedEffect(Unit) {
        WalletConnectModalDelegate
            .wcEventModels
            .onEach { event -> event?.handleEvent(context, closeModal) }
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
                (fadeIn() + slideInVertically(animationSpec = tween(400),
                    initialOffsetY = { fullHeight -> fullHeight })).togetherWith(fadeOut(animationSpec = tween(200)))
            }
        ) { state ->
            when (state) {
                WalletConnectModalState.Loading -> LoadingModalState()
                is WalletConnectModalState.Connect -> ModalNavGraph(
                    navController = navController,
                    state = state,
                    retry = viewModel::retry
                )
                is WalletConnectModalState.Error -> ErrorModalState(closeModal)
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

@Composable
private fun ErrorModalState(closeModal: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Something goes wrong", style = TextStyle(color = ModalTheme.colors.onBackgroundColor, fontSize = 18.sp))
        VerticalSpacer(height = 10.dp)
        RoundedMainButton(
            text = "Close",
            onClick = { closeModal() },
        )
    }
}

private fun Modal.Model.handleEvent(context: Context, closeModal: () -> Unit) {
    when (this) {
        is Modal.Model.ApprovedSession -> {
            closeModal()
        }
        is Modal.Model.Error -> {
            Toast.makeText(context, "Something go wrong", Toast.LENGTH_SHORT).show()
        }
        else -> Unit
    }
}
