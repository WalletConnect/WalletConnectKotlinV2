@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)

package com.walletconnect.modal.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.walletconnect.modal.ui.components.ModalRoot
import com.walletconnect.modal.ui.navigation.ModalNavGraph
import com.walletconnect.modal.ui.navigation.Route
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
internal fun WalletConnectModal(
    navController: NavController,
    sheetState: ModalBottomSheetState
) {
    LaunchedEffect(sheetState.currentValue) {
        if (sheetState.currentValue == ModalBottomSheetValue.Hidden) {
            if (navController.currentDestination?.route?.contains(Route.WalletConnectModalRoot.path) == true) {
                navController.popBackStack()
            }
        }
    }

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

    state?.let {
        ModalRoot(
            navController = navController,
            closeModal = closeModal
        ) {
            ModalNavGraph(
                navController = navController,
                state = it
            )
        }
    }
}

private fun WalletConnectModalEvents.handleEvent(
    context: Context,
    closeModal: () -> Unit
) {
    when(this) {
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
