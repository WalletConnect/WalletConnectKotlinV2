package com.walletconnect.web3.modal.ui.routes.account

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.walletconnect.web3.modal.ui.navigation.ConsumeNavigationEventsEffect
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.navigation.account.chainSwitchRoute
import com.walletconnect.web3.modal.ui.routes.account.account.AccountRoute
import com.walletconnect.web3.modal.ui.routes.account.change_network.ChangeNetworkRoute
import com.walletconnect.web3.modal.ui.routes.account.what_is_network.WhatIsNetworkRoute
import com.walletconnect.web3.modal.ui.utils.AnimatedNavGraph
import com.walletconnect.web3.modal.ui.utils.animatedComposable

@Composable
internal fun AccountNavGraph(
    navController: NavHostController,
    closeModal: () -> Unit,
    shouldOpenChangeNetwork: Boolean
) {
    val startDestination = if (shouldOpenChangeNetwork) Route.CHANGE_NETWORK.path else Route.ACCOUNT.path
    val accountViewModel = viewModel<AccountViewModel>()

    ConsumeNavigationEventsEffect(
        navController = navController,
        navigator = accountViewModel,
        closeModal = closeModal
    )

    AnimatedNavGraph(
        navController = navController,
        startDestination = startDestination
    ) {
        animatedComposable(route = Route.ACCOUNT.path) {
            AccountRoute(
                accountViewModel = accountViewModel,
                navController = navController
            )
        }
        animatedComposable(route = Route.CHANGE_NETWORK.path) {
            ChangeNetworkRoute(accountViewModel = accountViewModel)
        }
        animatedComposable(route = Route.WHAT_IS_WALLET.path) {
            WhatIsNetworkRoute()
        }
        chainSwitchRoute(accountViewModel = accountViewModel)
    }
}
