package com.walletconnect.web3.modal.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.walletconnect.web3.modal.ui.Web3ModalState
import com.walletconnect.web3.modal.ui.routes.session.SessionRoute

internal fun NavGraphBuilder.sessionModalGraph(
    navController: NavController,
    web3ModalState: Web3ModalState.SessionState
) {
    composable(route = Route.SESSION.path) {
        SessionRoute()
    }
}
