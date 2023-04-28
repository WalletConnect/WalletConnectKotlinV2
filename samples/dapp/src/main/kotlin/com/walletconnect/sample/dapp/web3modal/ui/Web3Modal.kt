@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class,
    ExperimentalMaterialNavigationApi::class
)

package com.walletconnect.sample.dapp.web3modal.ui

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.walletconnect.sample.dapp.web3modal.ui.common.Web3ModalRoot
import com.walletconnect.sample.dapp.web3modal.ui.theme.Web3ModalColors
import com.walletconnect.sample.dapp.web3modal.ui.theme.provideDefaultColors
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal const val uriArgId = "uriArgId"
fun NavController.navigateToWeb3Modal(uri: String) {
    // that uri parameter maybe should be changed into chains list and move connect logic to Web3ModalViewModel
    navigate(Route.Web3Modal.path + "/${Uri.encode(uri)}")
}
@OptIn(ExperimentalMaterialNavigationApi::class)
fun NavGraphBuilder.web3ModalGraph(sheetState: ModalBottomSheetState) {
    bottomSheet(
        route = Route.Web3Modal.path + "/{$uriArgId}",
        arguments = listOf(
            navArgument(uriArgId) { type = NavType.StringType }
        )
    ) {
        Web3Modal(sheetState = sheetState)
    }
}

@Composable
fun Web3Modal(
    sheetState: ModalBottomSheetState,
    colors: Web3ModalColors = provideDefaultColors(),
) {
    val web3ModalViewModel: Web3ModalViewModel = viewModel()
    val navController = rememberAnimatedNavController()
    val coroutinesScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        web3ModalViewModel
            .walletEvents
            .onEach { event ->
                when(event) {
                    Web3ModalEvents.SessionApproved -> coroutinesScope.launch { sheetState.hide() }
                    Web3ModalEvents.SessionRejected -> Toast.makeText(context, "Session was rejected", Toast.LENGTH_SHORT).show()
                    Web3ModalEvents.NoAction -> Unit
                }
            }
            .collect()
    }

    Web3ModalRoot(
        sheetState = sheetState,
        coroutinesScope = coroutinesScope,
        navController = navController,
        colors = colors
    ) {
        Web3ModalNavGraph(
            navController = navController,
            web3ModalViewModel = web3ModalViewModel
        )
    }
}

