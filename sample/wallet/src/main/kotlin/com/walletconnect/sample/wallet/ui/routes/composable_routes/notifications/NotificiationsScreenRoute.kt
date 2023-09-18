package com.walletconnect.sample.wallet.ui.routes.composable_routes.notifications

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.walletconnect.sample.common.ui.theme.PreviewTheme
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.domain.model.NotificationUI
import com.walletconnect.sample.wallet.ui.routes.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import com.walletconnect.sample.common.R as CommonR

@Composable
fun NotificationsScreenRoute(navController: NavHostController, subscriptionTopic: String, subscriptionDappName: String, subscriptionDappIcon: String) {
    val viewModel: NotificationsViewModel = viewModel()
    val state by viewModel.test.collectAsState()

    NotificationScreen(
        dappName = subscriptionDappName,
        dappIcon = subscriptionDappIcon,
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

    LaunchedEffect(Unit) {
        viewModel.setSubscriptionTopic(subscriptionTopic)
    }
}

@Composable
private fun NotificationScreen(
    dappName: String,
    dappIcon: String,
    state: NotificationsState,
    onNotificationItemDelete: (NotificationUI) -> Unit,
    onBackClick: () -> Unit,
    onNotificationSettings: () -> Unit,
    onUnsubscribe: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxHeight()) {
        var isMoreExpanded by remember { mutableStateOf(false) }

        TaskBar(
            dappIconUrl = dappIcon, dappName = dappName, isMoreExpanded,
            onBackIconClick = onBackClick, onMoreIconClick = { isMoreExpanded = !isMoreExpanded },
            onNotificationSettings = onNotificationSettings, onUnsubscribe = onUnsubscribe
        )

        if (state is NotificationsState.Success) {
            val lazyListState = rememberLazyListState()

            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
            ) {
                items(state.notifications, key = { it.id }) { notificationUI ->
                    NotificationItem(notificationUI, onNotificationItemDelete)
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

@Composable
fun MoreMenu(
    modifier: Modifier,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onNotificationSettings: () -> Unit,
    onUnsubscribe: () -> Unit,
) {
    Box(modifier = modifier.wrapContentSize(Alignment.TopStart)) {
        Image(
            painter = painterResource(id = R.drawable.ic_more),
            contentDescription = "More",
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = Modifier
                .width(215.dp)
                .padding(start = 5.dp, top = 5.dp, end = 5.dp, bottom = 5.dp)
        ) {
            DropdownMenuItem(onClick = {
                onDismissRequest()

                onNotificationSettings()
            }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painter = painterResource(R.drawable.ic_notifications_settings), contentDescription = "Notifications settings")
                    Text(
                        text = "Notification Preferences",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight(700),
                            color = MaterialTheme.colors.onSurface,
                            textAlign = TextAlign.Center,
                        )
                    )
                }

            }
            DropdownMenuItem(onClick = {
                onDismissRequest()
                onUnsubscribe()
            }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painter = painterResource(R.drawable.ic_unsubscribe), contentDescription = "Notifications settings", tint = Color(0xFFF05142))
                    Text(
                        text = "Unsubscribe",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight(700),
                            color = Color(0xFFF05142),
                            textAlign = TextAlign.Center,
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TaskBar(
    dappIconUrl: String,
    dappName: String,
    isMoreExpanded: Boolean,
    onBackIconClick: () -> Unit,
    onMoreIconClick: () -> Unit,
    onNotificationSettings: () -> Unit,
    onUnsubscribe: () -> Unit,
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(30.dp))
    ) {
        val startGuideline = createGuidelineFromStart(10.dp)

        val (
            backIconRef,
            iconRef,
            titleRef,
            moreRef,
        ) = createRefs()

        Icon(
            modifier = Modifier
                .constrainAs(backIconRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(startGuideline)
                    width = Dimension.value(12.dp)
                    height = Dimension.value(12.dp)
                }
                .clickable { onBackIconClick() },
            tint = MaterialTheme.colors.onBackground,
            imageVector = ImageVector.vectorResource(id = CommonR.drawable.chevron_left),
            contentDescription = "BackArrow",
        )

        AsyncImage(
            modifier = Modifier
                .constrainAs(iconRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(backIconRef.end, 8.dp)
                    width = Dimension.value(32.dp)
                    height = Dimension.value(32.dp)
                },
            model = ImageRequest.Builder(LocalContext.current)
                .data(dappIconUrl)
                .scale(Scale.FILL)
                .crossfade(true)
                .placeholder(R.drawable.sad_face)
                .build(),
            contentDescription = null,
        )

        Text(
            modifier = Modifier
                .constrainAs(titleRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(iconRef.end, 8.dp)
                    end.linkTo(titleRef.start, 8.dp)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                    verticalChainWeight = .5f
                },
            text = dappName,
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 23.4.sp,
                fontWeight = FontWeight(600),
                color = MaterialTheme.colors.onSurface,
            )
        )

        MoreMenu(modifier = Modifier
            .constrainAs(moreRef) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end, 8.dp)
                width = Dimension.wrapContent
                height = Dimension.wrapContent
                verticalChainWeight = .5f
            }
            .clickable { onMoreIconClick() },
            expanded = isMoreExpanded,
            onDismissRequest = onMoreIconClick,
            onNotificationSettings = onNotificationSettings,
            onUnsubscribe = onUnsubscribe
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationItem(
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
                Notification(false, notification)
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
fun Notification(isUnread: Boolean = false, notificationUI: NotificationUI) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = MaterialTheme.colors.background)
    ) {
        val (
            unreadIndicator,
            avatarIcon,
            notificationTitle,
            timestamp,
            notificationBody,
        ) = createRefs()

        if (isUnread) {
            Box(
                modifier = Modifier
                    .constrainAs(unreadIndicator) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start, 8.dp)
                        width = Dimension.value(8.dp)
                        height = Dimension.value(8.dp)
                        verticalChainWeight = .5f
                    }
                    .clip(CircleShape)
                    .background(color = Color(0xFF3396FF))
            )
        }

        AsyncImage(
            modifier = Modifier
                .constrainAs(avatarIcon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, 22.dp)
                    width = Dimension.value(64.dp)
                    height = Dimension.value(64.dp)
                    verticalChainWeight = .5f
                }
                .border(width = 1.dp, color = Color(0x1A062B2B), shape = RoundedCornerShape(size = 10.dp)),
            model = ImageRequest.Builder(LocalContext.current)
                .data(notificationUI.icon)
                .scale(Scale.FILL)
                .crossfade(true)
                .placeholder(R.drawable.green_check)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )

        Text(
            modifier = Modifier
                .constrainAs(notificationTitle) {
                    top.linkTo(parent.top)
                    bottom.linkTo(notificationBody.top)
                    start.linkTo(avatarIcon.end, 12.dp)
                    end.linkTo(timestamp.start, 5.dp)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                    verticalChainWeight = .0f
                    horizontalChainWeight = 0f
                },
            text = notificationUI.title,
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight(700),
                color = MaterialTheme.colors.onSurface,
            )
        )

        Text(
            modifier = Modifier
                .constrainAs(timestamp) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                    verticalChainWeight = 0f
                    horizontalChainWeight = 1f
                },
            text = notificationUI.date,
            style = TextStyle(
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight(500),
                color = MaterialTheme.colors.onSurface,
                letterSpacing = 0.12.sp,
            )
        )

        Text(
            modifier = Modifier
                .constrainAs(notificationBody) {
                    top.linkTo(notificationTitle.bottom, 2.dp)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(avatarIcon.end, 12.dp)
                    end.linkTo(parent.end, 4.dp)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                    verticalChainWeight = 0f
                    horizontalChainWeight = 0f
                },
            text = notificationUI.body,
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight(400),
                color = MaterialTheme.colors.onSurface,
            ),
            maxLines = 2
        )
    }
}

//@CompletePreviews
@Preview
@Composable
private fun NotificationsScreenPreview(
//    @PreviewParameter(NotificationsScreenStateProvider::class) state: NotificationsState,
) {
    PreviewTheme {
        NotificationScreen(
            "Dapp Name",
            "",
            state = NotificationsState.Empty,
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
                    NotificationUI("1", "topic1", "10-10-2023", "Title 1", "Body 1", null, null),
//                    NotifyNotification("2", "topic2", "03-02-2023", "Title 2", "Body 2", null, null),
//                    NotifyNotification("3", "topic3", "31-01-2023", "Title 3", "Body 3", null, null),
//                    NotifyNotification("4", "topic4", "02-07-2022", "Title 4", "Body 4", null, null),
                )
            )
        )
}
