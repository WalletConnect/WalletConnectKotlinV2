@file:OptIn(ExperimentalMaterialNavigationApi::class)

package com.walletconnect.web3.modal.ui

import androidx.annotation.IdRes
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.fragment.dialog
import androidx.navigation.navArgument
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalComponent
import com.walletconnect.web3.modal.ui.navigation.Route

internal const val CHOOSE_NETWORK_KEY = "chooseNetwork"
private const val CHOOSE_NETWORK_ARG = "{chooseNetwork}"

fun NavGraphBuilder.web3Modal() {
    dialog<Web3ModalSheet>(Route.WEB3MODAL.path)
}

fun NavController.openWeb3Modal(@IdRes id: Int) = navigate(id)

fun NavController.openWeb3Modal(
    shouldOpenChooseNetwork: Boolean = false
) = navigate(Route.WEB3MODAL.path + "/$shouldOpenChooseNetwork")

fun NavGraphBuilder.web3ModalGraph(navController: NavController) {
    bottomSheet(
        route = Route.WEB3MODAL.path + "/$CHOOSE_NETWORK_ARG",
        arguments = listOf(navArgument(CHOOSE_NETWORK_KEY) { type = NavType.BoolType })
    ) {
        Web3Modal(navController = navController)
    }
}

@Composable
internal fun Web3Modal(
    navController: NavController
) {
    Web3ModalComponent(closeModal = navController::popBackStack)
}
