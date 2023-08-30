package com.walletconnect.web3.modal.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.walletconnect.web3.modal.ui.Web3ModalState
import com.walletconnect.web3.modal.ui.routes.account.AccountRoute
import com.walletconnect.web3.modal.ui.routes.account.ChangeNetworkRoute

internal fun NavGraphBuilder.accountModalGraph(
    navController: NavController,
    web3ModalState: Web3ModalState.AccountState,
    closeModal: () -> Unit
) {
    composable(route = Route.ACCOUNT.path) {
        AccountRoute(
            navController = navController,
            closeModal = closeModal
        )
    }
    composable(route = Route.CHANGE_NETWORK.path) {
        ChangeNetworkRoute()
    }
}
