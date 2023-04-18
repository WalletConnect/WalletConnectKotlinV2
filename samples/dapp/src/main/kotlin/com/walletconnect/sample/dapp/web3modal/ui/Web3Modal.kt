@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class,
    ExperimentalMaterialNavigationApi::class
)

package com.walletconnect.sample.dapp.web3modal.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.walletconnect.sample.dapp.web3modal.ui.common.Web3ModalRoot
import com.walletconnect.sample.dapp.web3modal.ui.theme.Web3ModalColors
import com.walletconnect.sample.dapp.web3modal.ui.theme.provideDefaultColors

fun NavController.navigateToWeb3Modal() {
    navigate(Route.Web3Modal.path)
}
fun NavGraphBuilder.web3ModalGraph(sheetState: ModalBottomSheetState) {
    bottomSheet(route = Route.Web3Modal.path) {
        Web3Modal(sheetState = sheetState)
    }
}

@Composable
fun Web3Modal(
    sheetState: ModalBottomSheetState,
    colors: Web3ModalColors = provideDefaultColors(),
) {
    val navController = rememberAnimatedNavController()
    val coroutinesScope = rememberCoroutineScope()

    Web3ModalRoot(
        sheetState = sheetState,
        coroutinesScope = coroutinesScope,
        navController = navController,
        colors = colors
    ) {
        Web3ModalNavGraph(navController = navController)
    }
}

