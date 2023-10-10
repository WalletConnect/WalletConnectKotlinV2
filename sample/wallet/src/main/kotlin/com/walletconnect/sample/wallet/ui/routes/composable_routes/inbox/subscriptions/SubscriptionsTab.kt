package com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.subscriptions


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.SubscriptionsState


@Composable
fun SubscriptionsTab(
    navController: NavHostController,
    state: SubscriptionsState,
    activeSubscriptions: List<ActiveSubscriptionsUI>,
    onDiscoverMoreClicked: () -> Unit,
) {
    when (state) {
        is SubscriptionsState.Failure -> {

        }

        is SubscriptionsState.Unsubscribed -> {
            NoActiveSubscriptions(onDiscoverMoreClicked)
        }

        is SubscriptionsState.Searching -> {

        }

        SubscriptionsState.Success -> {
            ActiveSubscriptions(navController, activeSubscriptions)
        }
    }
}