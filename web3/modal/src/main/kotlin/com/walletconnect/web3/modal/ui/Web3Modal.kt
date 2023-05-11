@file:OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterialNavigationApi::class
)

package com.walletconnect.web3.modal.ui

import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.walletconnect.web3.modal.domain.configuration.CONFIGURATION
import com.walletconnect.web3.modal.domain.configuration.Config
import com.walletconnect.web3.modal.domain.configuration.Web3ModalConfigSerializer
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalRoot
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.navigation.Web3ModalNavGraph
import com.walletconnect.web3.modal.ui.theme.Web3ModalColors
import com.walletconnect.web3.modal.ui.theme.provideWeb3ModalColors
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

fun NavController.navigateToWeb3Modal(config: Config) {
    navigate(Route.Web3Modal.path + "/${config.parse()}")
}

fun NavGraphBuilder.web3ModalGraph(sheetState: ModalBottomSheetState) {
    bottomSheet(
        route = Route.Web3Modal.path + "/{$CONFIGURATION}",
    ) {
        Web3Modal(sheetState = sheetState)
    }
}

@Composable
internal fun Web3Modal(
    sheetState: ModalBottomSheetState,
    colors: Web3ModalColors = provideWeb3ModalColors()
) {
    val web3ModalViewModel: Web3ModalViewModel = viewModel()
    val navController = rememberAnimatedNavController()
    val coroutinesScope = rememberCoroutineScope()
    val context = LocalContext.current
    val web3ModalState by web3ModalViewModel.modalState.collectAsState()

    LaunchedEffect(Unit) {
        web3ModalViewModel
            .modalEvents
            .onEach { event ->
                when(event) {
                    Web3ModalEvents.SessionApproved -> {
                        coroutinesScope.launch { sheetState.hide() }
                        Toast.makeText(context, "Session was approved", Toast.LENGTH_SHORT).show()
                    }
                    Web3ModalEvents.SessionRejected -> Toast.makeText(context, "Session was rejected", Toast.LENGTH_SHORT).show()
                    Web3ModalEvents.NoAction -> Unit
                    Web3ModalEvents.InvalidState -> coroutinesScope.launch { sheetState.hide() }
                }
            }
            .collect()
    }

    web3ModalState?.let { state ->
        Web3ModalRoot(
            sheetState = sheetState,
            coroutineScope = coroutinesScope,
            navController = navController,
            colors = colors
        ) {
            Web3ModalNavGraph(
                navController = navController,
                web3ModalState = state,
            )
        }
    }
}

private fun Config.parse() = Web3ModalConfigSerializer.serialize(this)