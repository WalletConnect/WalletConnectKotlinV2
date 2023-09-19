package com.walletconnect.web3.modal.ui.routes.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.routes.account.account.AccountRoute
import com.walletconnect.web3.modal.ui.routes.account.change_network.ChangeNetworkRoute

@Composable
internal fun AccountNavGraph(
    navController: NavHostController,
    closeModal: () -> Unit,
    shouldOpenChangeNetwork: Boolean
) {
    val accountState = rememberAccountState(
        coroutineScope = rememberCoroutineScope(),
        navController = navController
    )
    val startDestination = if (shouldOpenChangeNetwork) Route.CHANGE_NETWORK.path else Route.ACCOUNT.path
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Route.ACCOUNT.path) {
            AccountRoute(
                accountState = accountState,
                navController = navController,
                closeModal = closeModal
            )
        }
        composable(route = Route.CHANGE_NETWORK.path) {
            ChangeNetworkRoute(accountState = accountState)
        }
        composable(route = Route.CHOOSE_NETWORK.path) {}
    }
}