package com.walletconnect.sample.wallet.ui.routes.composable_routes.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.domain.model.PushNotification
import com.walletconnect.sample.wallet.ui.common.WCTopAppBar
import com.walletconnect.sample.wallet.ui.routes.showSnackbar

@Composable
fun NotificationsScreenRoute(navController: NavHostController) {
    val viewModel: NotificationsViewModel = viewModel()
    val state by viewModel.notificationsState.collectAsState()
    NotificationScreen(
        state = NotificationsState.Success(
            listOf(
                PushNotification("Title", "Message", "URL", "10.10.10"),
                PushNotification("Title", "Message", "URL", "10.10.10"),
                PushNotification("Title", "Message", "URL", "10.10.10"),
                PushNotification("Title", "Message", "URL", "10.10.10"),
            )
        ),
        onNotificationItemDelete = {},
        onShowSnackBar = { navController.showSnackbar(it) },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
private fun NotificationScreen(
    state: NotificationsState,
    onNotificationItemDelete: () -> Unit,
    onShowSnackBar: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Column {
        WCTopAppBar(text = "Notifications", canGoBack = true, onBackIconClick = onBackClick)
        LazyColumn(modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp)) {
            notificationsContent(state, onNotificationItemDelete, onShowSnackBar)
        }
    }
}

private fun LazyListScope.notificationsContent(
    state: NotificationsState,
    onNotificationItemDelete: () -> Unit,
    onError: (String) -> Unit
) {
    when (state) {
        NotificationsState.Empty -> item { EmptyState() }
        NotificationsState.Error -> {
            onError("Something goes wrong")
            item { EmptyState() }
        }
        is NotificationsState.Success -> items(state.notifications) { item ->
            NotificationItem(item, onNotificationItemDelete)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "No notifications")
    }
}

@Composable
private fun NotificationItem(
    item: PushNotification,
    onNotificationItemDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = Color(0xFF9EA9A9),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            NotificationDetails(
                item = item,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            TrashButton(
                item = item,
                onNotificationItemDelete = onNotificationItemDelete
            )
        }
    }
}

@Composable
private fun NotificationDetails(item: PushNotification, modifier: Modifier) {
    Column(modifier) {
        Text(text = item.title)
        Text(text = item.message)
        Text(text = item.date)
        Text(text = item.url)
    }
}

@Composable
private fun TrashButton(
    item: PushNotification,
    onNotificationItemDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFF3E0516),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
            .clickable { onNotificationItemDelete() }
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_trash),
            contentDescription = "Notifications Icon",
            tint = Color(0xFFC5325E)
        )
    }
}
