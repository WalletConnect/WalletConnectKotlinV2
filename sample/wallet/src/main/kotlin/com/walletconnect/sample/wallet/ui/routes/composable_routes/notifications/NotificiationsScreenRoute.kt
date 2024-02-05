package com.walletconnect.sample.wallet.ui.routes.composable_routes.notifications

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.walletconnect.sample.common.ui.commons.ButtonWithLoader
import com.walletconnect.sample.common.ui.theme.PreviewTheme
import com.walletconnect.sample.common.ui.theme.UiModePreview
import com.walletconnect.sample.common.ui.theme.blue_accent
import com.walletconnect.sample.wallet.domain.model.NotificationUI
import com.walletconnect.sample.wallet.ui.common.ImageUrl
import com.walletconnect.sample.wallet.ui.common.subscriptions.ActiveSubscriptionsUI
import com.walletconnect.sample.wallet.ui.common.subscriptions.NotificationIcon
import com.walletconnect.sample.wallet.ui.routes.Route
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.InboxViewModel
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.LazyColumnSurroundedWithFogVertically
import com.walletconnect.sample.wallet.ui.routes.showSnackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Composable
fun NotificationsScreenRoute(navController: NavHostController, subscriptionTopic: String, inboxViewModel: InboxViewModel) {
    val viewModel: NotificationsViewModel = runCatching { viewModel<NotificationsViewModel>(key = subscriptionTopic, factory = NotificationsViewModelFactory(subscriptionTopic)) }
        .getOrElse {
            navController.popBackStack()
            return navController.showSnackbar("Active subscription no longer exists")
        }

    val state by viewModel.state.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val scrollToTopCounter by viewModel.scrollToTopCounter.collectAsState()
    val currentSubscription by viewModel.currentSubscription.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val scope = rememberCoroutineScope()

    NotificationScreen(currentSubscription = currentSubscription, state = state, notifications = notifications.toList(), onBackClick = { navController.popBackStack() }, onNotificationSettings = {
        navController.navigate("${Route.UpdateSubscription.path}/$subscriptionTopic")
    }, onUnsubscribe = {
        viewModel.unsubscribe(onSuccess = {
            scope.launch {
                inboxViewModel.activeSubscriptions.onEach { subscriptionsUIList ->
                    if (subscriptionsUIList.find { subscriptionsUI -> subscriptionsUI == currentSubscription } == null) {
                        navController.popBackStack()
                        cancel()
                    }
                }.launchIn(scope)
                inboxViewModel.fetchActiveSubscriptions()
            }
        }, onFailure = {
            scope.launch {
                val message = "Unable to unsubscribe. Reason: ${it.message}"
                Timber.e(message)
                navController.showSnackbar(message)
            }
        })
    }, onRetry = viewModel::retryFetchingAllNotifications, onFetchMore = viewModel::fetchMore, hasMore = hasMore, scrollToTopCounter = scrollToTopCounter
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.fetchRecentNotifications()
        }
    }
}

@Composable
private fun NotificationScreen(
    currentSubscription: ActiveSubscriptionsUI,
    state: NotificationsState,
    scrollToTopCounter: Int,
    notifications: List<NotificationUI>,
    onBackClick: () -> Unit,
    onNotificationSettings: () -> Unit,
    onUnsubscribe: () -> Unit,
    onRetry: () -> Unit,
    onFetchMore: () -> Unit,
    hasMore: Boolean,
) {
    Column(modifier = Modifier.fillMaxHeight()) {
        var isMoreExpanded by remember { mutableStateOf(false) }

        if (state !is NotificationsState.Unsubscribing) {
            NotificationsHeader(
                currentSubscription,
                isMoreExpanded,
                onBackIconClick = onBackClick,
                onMoreIconClick = { isMoreExpanded = !isMoreExpanded },
                onNotificationSettings = onNotificationSettings,
                onUnsubscribe = onUnsubscribe
            )
        }

        when (state) {
            is NotificationsState.Success, is NotificationsState.IncomingNotifications, is NotificationsState.FetchingMore -> {
                AnimatedVisibility(
                    visible = state is NotificationsState.IncomingNotifications,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Fetching incoming notifications...", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight(600), color = blue_accent))
                    }
                }

                if (notifications.isEmpty()) {
                    EmptyOrLoadingOrFailureState("No notifications")
                } else {
                    val lazyListState = rememberLazyListState()

                    LazyColumnSurroundedWithFogVertically(lazyListState = lazyListState, indexByWhichShouldDisplayBottomFog = notifications.lastIndex - 5) {
                        itemsIndexed(notifications, key = { _, it -> it.id }) { index, notificationUI ->
                            NotificationItem(notificationUI)
                            if (index != notifications.lastIndex) Divider()
                        }
                        if (hasMore) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.Center
                                ) {
                                    ButtonWithLoader("Fetch more", onClick = {
                                        onFetchMore()
                                    }, isLoading = state is NotificationsState.FetchingMore)
                                }
                            }
                        }
                    }

                    LaunchedEffect(key1 = scrollToTopCounter) {
                        withContext(Dispatchers.Main) {
                            lazyListState.scrollToItem(0)
                        }
                    }
                }
            }

            is NotificationsState.Failure -> {
                EmptyOrLoadingOrFailureState(text = "${state.error.message}") {
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedButton(shape = CircleShape, colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background), onClick = { onRetry() }) {
                        Text(
                            text = "Retry", style = TextStyle(
                                fontSize = 14.sp, fontWeight = FontWeight(600), textAlign = TextAlign.Center, color = MaterialTheme.colors.onBackground
                            )
                        )
                    }
                }

            }

            NotificationsState.InitialFetching -> {
                EmptyOrLoadingOrFailureState(text = "Fetching notifications...") {
                    Spacer(modifier = Modifier.height(20.dp))
                    CircularProgressIndicator(modifier = Modifier.size(80.dp), color = blue_accent)
                }
            }

            NotificationsState.Unsubscribing -> {
                EmptyOrLoadingOrFailureState(text = "Unsubscribing...") {
                    Spacer(modifier = Modifier.height(20.dp))
                    CircularProgressIndicator(modifier = Modifier.size(80.dp), color = blue_accent)
                }
            }
        }
    }
}

@Composable
private fun EmptyOrLoadingOrFailureState(text: String, content: @Composable ColumnScope.() -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp), contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = text)
            content()
        }
    }
}

@Composable
fun NotificationItem(notificationUI: NotificationUI) {
    val unreadColor = if (isSystemInDarkTheme()) Color(0xFF0E0F0F) else Color(0xFFeef8ff)
    val context = LocalContext.current

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable {
            val parsedUrl = runCatching { Uri.parse(notificationUI.url) }.getOrNull()
            if (parsedUrl != null) {
                val intent = Intent(Intent.ACTION_VIEW, parsedUrl)
                context.startActivity(intent)
            }
        }
        .background(if (notificationUI.isUnread) unreadColor else MaterialTheme.colors.background)
        .padding(horizontal = 20.dp, vertical = 16.dp)) {
        NotificationIcon(48.dp, notificationUI.icon)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.weight(1f), text = notificationUI.title, style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight(590),
                    )
                )
                Text(
                    modifier = Modifier.wrapContentWidth(), text = notificationUI.date, style = TextStyle(
                        fontSize = 11.sp, fontWeight = FontWeight(510), color = MaterialTheme.colors.onBackground.copy(alpha = 0.3f)
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
                text = AnnotatedString("Show Less"), onClick = { isExpanded = false }, style = style.copy(color = blue_accent)
            )
        } else {
            Text(
                text = text, style = style, onTextLayout = onTextLayout, maxLines = maxLines, overflow = TextOverflow.Ellipsis
            )
            if (isOverflowed) {
                ClickableText(
                    text = AnnotatedString("Show More"), onClick = { isExpanded = true }, style = style.copy(color = blue_accent)
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
        NotificationScreen(ActiveSubscriptionsUI(
            "",
            ImageUrl("https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png", "", ""),
            "Dapp Name",
            0,
            "WalletConnect sample app for testing Notify features.",
            "",
            "gm.walletconnect.com",
            false,
            false
        ), state = state, notifications = listOf(
            NotificationUI("1", "topic1", "10-10-2023", 0L, "Title 1", "Body 1", null, null, true),
            NotificationUI("2", "topic2", "03-02-2023", 0L, "Title 2", "Body 2", null, null, false),
            NotificationUI("3", "topic3", "31-01-2023", 0L, "Title 3", "Body 3", null, null, true),
            NotificationUI("4", "topic4", "02-07-2022", 0L, "Title 4", "Body 4", null, null, false),
        ), onBackClick = {}, onNotificationSettings = {}, onUnsubscribe = {}, onRetry = {}, onFetchMore = {}, hasMore = true, scrollToTopCounter = 0
        )
    }
}

private class NotificationsScreenStateProvider : PreviewParameterProvider<NotificationsState> {
    override val values: Sequence<NotificationsState>
        get() = sequenceOf(
            NotificationsState.InitialFetching, NotificationsState.Success, NotificationsState.Failure(IllegalStateException("Failed to fetch notifications")), NotificationsState.Unsubscribing
        )
}
