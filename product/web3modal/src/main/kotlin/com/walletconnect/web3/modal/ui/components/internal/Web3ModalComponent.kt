package com.walletconnect.web3.modal.ui.components.internal

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.walletconnect.web3.modal.ui.Web3ModalEvents
import com.walletconnect.web3.modal.ui.Web3ModalViewModel
import com.walletconnect.web3.modal.ui.components.internal.root.Web3ModalRoot
import com.walletconnect.web3.modal.ui.navigation.Web3ModalNavGraph
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

// That may be public in the future to allow users use our composable view
@Composable
internal fun Web3ModalComponent(
    navController: NavHostController = rememberNavController(),
    closeModal: () -> Unit
) {
    val context = LocalContext.current
    val web3ModalViewModel: Web3ModalViewModel = viewModel()
    val web3ModalState by web3ModalViewModel.modalState.collectAsState()

    LaunchedEffect(Unit) {
        web3ModalViewModel
            .modalEvents
            .onEach { event ->
                when (event) {
                    Web3ModalEvents.SessionApproved -> {
                        closeModal()
                        Toast.makeText(context, "Session was approved", Toast.LENGTH_SHORT).show()
                    }

                    Web3ModalEvents.SessionRejected -> Toast.makeText(
                        context,
                        "Session was rejected",
                        Toast.LENGTH_SHORT
                    ).show()

                    Web3ModalEvents.NoAction -> Unit
                    Web3ModalEvents.InvalidState -> closeModal()
                }
            }
            .collect()
    }

    Web3ModalRoot(
        navController = navController,
        closeModal = closeModal
    ) {
        Web3ModalNavGraph(
            navController = navController,
            web3ModalState = web3ModalState,
        )
    }
}
