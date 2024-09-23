@file:OptIn(ExperimentalMaterialNavigationApi::class)

package com.walletconnect.web3.modal.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.fragment.dialog
import androidx.navigation.navArgument
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalComponent
import com.walletconnect.web3.modal.ui.navigation.Route

internal const val CHOOSE_NETWORK_KEY = "chooseNetwork"
private const val CHOOSE_NETWORK_ARG = "{chooseNetwork}"
private val web3ModalPath = Route.WEB3MODAL.path + "/" + CHOOSE_NETWORK_ARG

@Deprecated(
    "com.walletconnect.web3.modal.web3Modal has been deprecated. Please use com.reown.appkit.modal.ui.appKit instead from - https://github.com/reown-com/reown-kotlin",
    ReplaceWith("appKit()")
)
fun NavGraphBuilder.web3Modal() {
    dialog<Web3ModalSheet>(web3ModalPath) { argument(CHOOSE_NETWORK_KEY) { type = NavType.BoolType } }
}

@Deprecated(
    "com.walletconnect.web3.modal.openWeb3Modal has been deprecated. Please use com.reown.appkit.modal.ui.openAppKit instead from - https://github.com/reown-com/reown-kotlin",
    ReplaceWith("openAppKit(shouldOpenChooseNetwork, onError")
)
@SuppressLint("RestrictedApi")
fun NavController.openWeb3Modal(
    shouldOpenChooseNetwork: Boolean = false,
    onError: (Throwable) -> Unit = {}
) {
    when {
        findDestination(R.id.web3ModalGraph) != null -> {
            navigate(R.id.web3ModalGraph, args = Bundle().apply {
                putBoolean(CHOOSE_NETWORK_KEY, shouldOpenChooseNetwork)
            }, navOptions = buildWeb3ModalNavOptions())
        }

        findDestination(web3ModalPath) != null -> {
            navigate(
                route = Route.WEB3MODAL.path + "/$shouldOpenChooseNetwork",
                navOptions = buildWeb3ModalNavOptions()
            )
        }

        else -> onError(IllegalStateException("Invalid web3Modal path"))
    }
}

@Deprecated(
    "com.walletconnect.web3.modal.ui.NavGraphBuilder has been deprecated. Please use com.reown.appkit.modal.ui.NavGraphBuilder instead from - https://github.com/reown-com/reown-kotlin",
    ReplaceWith("appKitGraph(navController")
)
fun NavGraphBuilder.web3ModalGraph(navController: NavController) {
    bottomSheet(
        route = web3ModalPath,
        arguments = listOf(navArgument(CHOOSE_NETWORK_KEY) { type = NavType.BoolType })
    ) {
        val shouldOpenChooseNetwork = it.arguments?.getBoolean(CHOOSE_NETWORK_KEY) ?: false
        Web3Modal(
            navController = navController,
            shouldOpenChooseNetwork = shouldOpenChooseNetwork
        )
    }
}

private fun buildWeb3ModalNavOptions() = NavOptions.Builder().setLaunchSingleTop(true).build()

@Composable
internal fun Web3Modal(
    navController: NavController,
    shouldOpenChooseNetwork: Boolean
) {
    Web3ModalComponent(
        closeModal = navController::popBackStack,
        shouldOpenChooseNetwork = shouldOpenChooseNetwork
    )
}
