package com.walletconnect.sample.wallet.ui.routes.composable_routes.notifications

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.skydoves.landscapist.glide.GlideImage
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.domain.model.PushNotification
import com.walletconnect.sample.wallet.ui.common.CompletePreviews
import com.walletconnect.sample.wallet.ui.common.WCTopAppBar

@Composable
fun NotificationsScreenRoute(navController: NavHostController) {
    val viewModel: NotificationsViewModel = viewModel()
    val state by viewModel.notificationsState.collectAsState()
    NotificationScreen(
        state = state,
        onNotificationItemDelete = viewModel::deleteNotification,
        onBackClick = { navController.popBackStack() }
    )

    LaunchedEffect(Unit) {
        viewModel.getMessageHistory()
    }
}

@Composable
private fun NotificationScreen(
    state: NotificationsState,
    onNotificationItemDelete: (PushNotification) -> Unit,
    onBackClick: () -> Unit
) {
    Column {
        WCTopAppBar(text = "Notifications", canGoBack = true, onBackIconClick = onBackClick)
        LazyColumn(modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp)) {
            notificationsContent(state, onNotificationItemDelete)
        }
    }
}

private fun LazyListScope.notificationsContent(
    state: NotificationsState,
    onNotificationItemDelete: (PushNotification) -> Unit,
) {
    when (state) {
        NotificationsState.Empty -> item { EmptyState() }
        is NotificationsState.Success -> items(state.notifications, key = {it.id}) { item ->
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.NotificationItem(
    item: PushNotification,
    onNotificationItemDelete: (PushNotification) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = Color(0xFF9EA9A9),
                shape = RoundedCornerShape(12.dp)
            )
            .animateItemPlacement()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item.icon?.let {
                GlideImage(
                    imageModel = { it },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                )
            }
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
        Text(text = item.body)
        item.url?.let { Text(text = it) }
    }
}

@Composable
private fun TrashButton(
    item: PushNotification,
    onNotificationItemDelete: (PushNotification) -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFF3E0516),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
            .clickable { onNotificationItemDelete(item) }
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_trash),
            contentDescription = "Notifications Icon",
            tint = Color(0xFFC5325E)
        )
    }
}

@CompletePreviews
@Composable
private fun NotificationsScreenPreview(
    @PreviewParameter(NotificationsScreenStateProvider::class) state: NotificationsState
) {
        NotificationScreen(
            state = state,
            onNotificationItemDelete = {},
            onBackClick = {}
        )
}

private class NotificationsScreenStateProvider : PreviewParameterProvider<NotificationsState> {
    override val values: Sequence<NotificationsState>
        get() = sequenceOf(
            NotificationsState.Empty,
            NotificationsState.Success(listOf(
                    PushNotification("1", "topic1", "10-10-2023", "Title 1", "Body 1", null, null),
                    PushNotification("2", "topic2", "03-02-2023", "Title 2", "Body 2", null, null),
                    PushNotification("3", "topic3", "31-01-2023", "Title 3", "Body 3", null, null),
                    PushNotification("4", "topic4", "02-07-2022", "Title 4", "Body 4", null, null),
            ))
        )
}
