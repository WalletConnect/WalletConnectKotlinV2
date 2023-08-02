@file:OptIn(ExperimentalMaterialNavigationApi::class)

package com.walletconnect.wcmodal.ui

import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.fragment.dialog
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.walletconnect.wcmodal.ui.navigation.Route

fun NavGraphBuilder.walletConnectModal() {
    dialog<WalletConnectModalSheet>(route = Route.WalletConnectModalRoot.path)
}

fun NavController.openWalletConnectModal(@IdRes id: Int) = navigate(id)

fun NavController.openWalletConnectModal() = navigate(Route.WalletConnectModalRoot.path)

fun NavGraphBuilder.walletConnectModalGraph(navController: NavController) {
    bottomSheet(route = Route.WalletConnectModalRoot.path) {
        WalletConnectModal(navController = navController)
    }
}
