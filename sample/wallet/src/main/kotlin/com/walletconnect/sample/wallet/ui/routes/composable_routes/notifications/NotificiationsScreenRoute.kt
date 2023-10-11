package com.walletconnect.sample.wallet.ui.routes.composable_routes.notifications

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.sample.common.ui.theme.PreviewTheme
import com.walletconnect.sample.common.ui.theme.UiModePreview
import com.walletconnect.sample.common.ui.theme.blue_accent
import com.walletconnect.sample.wallet.domain.model.NotificationUI
import com.walletconnect.sample.wallet.ui.common.subscriptions.ActiveSubscriptionsUI
import com.walletconnect.sample.wallet.ui.routes.Route
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.LazyColumnSurroundedWithFogVertically
import com.walletconnect.sample.wallet.ui.routes.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun NotificationsScreenRoute(navController: NavHostController, subscriptionTopic: String) {
    val viewModel: NotificationsViewModel = runCatching {
        viewModel<NotificationsViewModel>(
            key = subscriptionTopic,
            factory = NotificationsViewModelFactory(subscriptionTopic)
        )
    }.getOrElse {
        navController.popBackStack()
        return navController.showSnackbar("Active subscription no longer exists")
    }

    val state by viewModel.state.collectAsState()
    val currentSubscription by viewModel.currentSubscription.collectAsState()

    NotificationScreen(
        currentSubscription = currentSubscription,
        state = state,
        onNotificationItemDelete = viewModel::deleteNotification,
        onBackClick = { navController.popBackStack() },
        onNotificationSettings = {
            navController.navigate("${Route.UpdateSubscription.path}/$subscriptionTopic")
        },
        onUnsubscribe = {
            viewModel.unsubscribe { Timber.e(it) }
            navController.popBackStack()
        },
    )
}

@Composable
private fun NotificationScreen(
    currentSubscription: ActiveSubscriptionsUI,
    state: NotificationsState,
    onNotificationItemDelete: (NotificationUI) -> Unit,
    onBackClick: () -> Unit,
    onNotificationSettings: () -> Unit,
    onUnsubscribe: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxHeight()) {
        var isMoreExpanded by remember { mutableStateOf(false) }

        NotificationsHeader(
            currentSubscription, isMoreExpanded,
            onBackIconClick = onBackClick, onMoreIconClick = { isMoreExpanded = !isMoreExpanded },
            onNotificationSettings = onNotificationSettings, onUnsubscribe = onUnsubscribe
        )

        if (state is NotificationsState.Success) {
            val lazyListState = rememberLazyListState()

            LazyColumnSurroundedWithFogVertically(indexByWhichShouldDisplayBottomFog = state.notifications.lastIndex - 5) {
                itemsIndexed(state.notifications, key = { _, it -> it.id }) { index, notificationUI ->
                    DissmisableNotificationItem(notificationUI, onNotificationItemDelete)
                    if (index != state.notifications.lastIndex) Divider()
                }

                CoroutineScope(Dispatchers.Main).launch {
                    lazyListState.scrollToItem(0)
                }
            }
        } else {
            EmptyState()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DissmisableNotificationItem(
    notification: NotificationUI,
    onRemove: (NotificationUI) -> Unit,
) {
    val context = LocalContext.current
    var show by remember { mutableStateOf(true) }
    val currentItem by rememberUpdatedState(notification)
    val dismissState = rememberDismissState(
        confirmValueChange = {
            if (it == DismissValue.DismissedToStart || it == DismissValue.DismissedToEnd) {
                show = false
                true
            } else false
        }, positionalThreshold = { 150.dp.toPx() }
    )
    AnimatedVisibility(
        show, exit = fadeOut(spring())
    ) {
        SwipeToDismiss(
            state = dismissState,
            modifier = Modifier,
            background = {
                DismissBackground(dismissState)
            },
            dismissContent = {
                NotificationItem2(notification)
            }, directions = setOf(DismissDirection.StartToEnd)

        )
    }

    LaunchedEffect(show) {
        if (!show) {
            delay(800)
            onRemove(currentItem)
            Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissBackground(dismissState: DismissState) {
    val color = when (dismissState.dismissDirection) {
        DismissDirection.StartToEnd -> Color(0xFFFA5959)
        DismissDirection.EndToStart -> Color.Transparent
        null -> Color.Transparent
    }
    val direction = dismissState.dismissDirection

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(12.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (direction == DismissDirection.StartToEnd) Icon(
            Icons.Default.Delete,
            contentDescription = "delete"
        )
        Spacer(modifier = Modifier)
    }
}

@Composable
fun NotificationItem2(notificationUI: NotificationUI) {
    val unreadColor = if (isSystemInDarkTheme()) Color(0xFF0E0F0F) else Color(0xFFeef8ff)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (notificationUI.isUnread) unreadColor else MaterialTheme.colors.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp)),
            model = ImageRequest.Builder(LocalContext.current)
                .data(notificationUI.icon)
                .crossfade(200)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = notificationUI.title,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight(590),
                    )
                )
                Text(
                    modifier = Modifier.wrapContentWidth(),
                    text = notificationUI.date,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight(510),
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.3f)
                    )
                )
                if (notificationUI.isUnread) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(color = blue_accent, shape = CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            ExpandableText(
                text = notificationUI.body,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight(400),
                ),
            )
        }

    }
}

@Composable
fun ExpandableText(
    text: String,
    style: TextStyle,
    maxLines: Int = 3,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isOverflowed by remember { mutableStateOf(false) }
    val onTextLayout = { textLayoutResult: TextLayoutResult -> isOverflowed = textLayoutResult.hasVisualOverflow }

    Column() {
        if (isExpanded) {
            Text(text = text, style = style)
            ClickableText(
                text = AnnotatedString("Show Less"),
                onClick = { isExpanded = false },
                style = style.copy(color = blue_accent)
            )
        } else {
            Text(
                text = text,
                style = style,
                onTextLayout = onTextLayout,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )
            if (isOverflowed) {
                ClickableText(
                    text = AnnotatedString("Show More"),
                    onClick = { isExpanded = true },
                    style = style.copy(color = blue_accent)
                )
            }
        }
    }
}

@Composable
@UiModePreview
private fun NotificationsScreenPreview(
    @PreviewParameter(NotificationsScreenStateProvider::class) state: NotificationsState,
) {
    PreviewTheme {
        NotificationScreen(
            ActiveSubscriptionsUI(
                "",
                "https://explorer-api.walletconnect.com/v3/logo/sm/ae213078-71b0-49ac-17e9-294719d92e00?projectId=8e998cd112127e42dce5e2bf74122539",
                "Dapp Name",
                0,
                "WalletConnect sample app for testing Notify features.",
                "",
                "gm.walletconnect.com",
                false,
                false
            ),
            state = state,
            onNotificationItemDelete = {},
            onBackClick = {},
            onNotificationSettings = {},
            onUnsubscribe = {}
        )
    }
}

private class NotificationsScreenStateProvider : PreviewParameterProvider<NotificationsState> {
    override val values: Sequence<NotificationsState>
        get() = sequenceOf(
            NotificationsState.Empty,
            NotificationsState.Success(
                listOf(
                    NotificationUI("1", "topic1", "10-10-2023", "Title 1", "Body 1", null, null, true),
                    NotificationUI("2", "topic2", "03-02-2023", "Title 2", "Body 2", null, null, false),
                    NotificationUI("3", "topic3", "31-01-2023", "Title 3", "Body 3", null, null, true),
                    NotificationUI("4", "topic4", "02-07-2022", "Title 4", "Body 4", null, null, false),
                )
            )
        )
}
