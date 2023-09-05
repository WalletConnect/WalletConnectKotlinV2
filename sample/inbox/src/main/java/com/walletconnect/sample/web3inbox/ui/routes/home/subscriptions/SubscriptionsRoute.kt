package com.walletconnect.sample.web3inbox.ui.routes.home.subscriptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walletconnect.notify.client.Notify
import com.walletconnect.sample.web3inbox.ui.routes.Route


@Composable
fun SubscriptionsRoute(navController: NavController, account: String) {
    val viewModel: SubscriptionsViewModel = viewModel()

    val state by viewModel.subscriptions.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(state.sub) { key, item ->
            SubscriptionItem(item) { navController.navigate(Route.Notification.path + "/${item.topic}") }
        }
    }
}

@Composable
fun SubscriptionItem(
    item: Notify.Model.Subscription,
    onClick: (Notify.Model.Subscription) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .clickable { onClick(item) }
    ) {
        Text(text = item.metadata.name)
        Text(text = item.metadata.description)
        Divider(modifier = Modifier.fillMaxWidth())
    }
}
