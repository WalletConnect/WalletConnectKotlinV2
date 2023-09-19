package com.walletconnect.sample.web3inbox.ui.routes.home.subscriptions.notifications

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walletconnect.notify.client.Notify


@Composable
fun NotificationsRoute(navController: NavController) {

    val viewModel: NotificationsViewModel = viewModel()

    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(state.notifications) { key, item ->
            NotificationItem(item)
        }
    }
}

@Composable
fun NotificationItem(item: Notify.Model.MessageRecord) {
    Column {
        Text(text = item.message.title)
        Text(text = item.message.body)
        Text(text = "${item.publishedAt}")
    }
}
