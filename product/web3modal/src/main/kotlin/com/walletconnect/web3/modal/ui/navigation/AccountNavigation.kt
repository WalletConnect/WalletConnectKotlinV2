package com.walletconnect.web3.modal.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.domain.model.Chain
import com.walletconnect.web3.modal.ui.Web3ModalState
import com.walletconnect.web3.modal.ui.routes.account.AccountRoute
import com.walletconnect.web3.modal.ui.routes.account.ChangeNetworkRoute

internal fun NavGraphBuilder.accountModalGraph(
    navController: NavController,
    web3ModalState: Web3ModalState.AccountState,
    disconnect: (String) -> Unit,
    closeModal: () -> Unit,
    changeChain: (AccountData, Chain) -> Unit
) {
    composable(route = Route.ACCOUNT.path) {
        AccountRoute(
            accountData = web3ModalState.accountData,
            navController = navController,
            disconnect = disconnect,
            closeModal = closeModal
        )
    }
    composable(route = Route.CHANGE_NETWORK.path) {
        ChangeNetworkRoute(
            accountData = web3ModalState.accountData,
            changeChain = changeChain
        )
    }
}
