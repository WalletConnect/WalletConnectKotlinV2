package com.walletconnect.sample.wallet.ui.routes.composable_routes.notifications

import android.content.Intent
import android.net.Uri
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
import com.skydoves.landscapist.glide.GlideImage
import com.walletconnect.sample.common.ui.theme.PreviewTheme
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.domain.model.NotificationUI
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
        onBackClick = { navController.popBackStack() }
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
) {
    Column {
        TaskBar(dappIconUrl = dappIcon, dappName = dappName, onBackIconClick = onBackClick)

        // TODO: Figure out if we still need these views in the new designs
        if (false) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(start = 20.dp, top = 5.dp, end = 20.dp, bottom = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier
                        .border(width = 1.dp, color = Color(0x1A000000), shape = RoundedCornerShape(size = 28.dp))
                        .width(58.dp)
                        .height(36.dp)
                        .background(color = Color(0xFFF1F3F3), shape = RoundedCornerShape(size = 28.dp))
                        .padding(start = 12.dp, top = 6.dp, end = 12.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier
                            .width(17.dp)
                            .height(17.dp),
                        text = "All",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight(700),
                            color = Color(0xFF272A2A),
                            textAlign = TextAlign.Center,
                        )
                    )

                    Icon(
                        modifier = Modifier
                            .width(12.dp)
                            .height(5.dp)
                            .padding(top = 1.dp),
                        painter = painterResource(id = R.drawable.ic_chevron_down),
                        contentDescription = null
                    )
                }

                Row(
                    modifier = Modifier
                        .border(width = 1.dp, color = Color(0x1A000000), shape = RoundedCornerShape(size = 28.dp))
                        .width(70.dp)
                        .height(36.dp)
                        .background(color = Color(0xFFF1F3F3), shape = RoundedCornerShape(size = 28.dp))
                        .padding(start = 12.dp, top = 6.dp, end = 12.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier
                            .width(46.dp)
                            .height(17.dp),
                        text = "Unread",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight(700),
                            color = Color(0xFF272A2A),
                            textAlign = TextAlign.Center,
                        )
                    )
                }
            }
        }

        if (state is NotificationsState.Success) {
            LazyColumn(
                contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
            ) {
                items(state.notifications, key = { it.id }) { notificationUI ->
                    Notification(
                        isUnread = false,
                        notificationUI = notificationUI
                    )
                }
            }
        } else {
            EmptyState()
        }
    }
}

private fun LazyListScope.notificationsContent(
    state: NotificationsState,
    onNotificationItemDelete: (NotificationUI) -> Unit,
) {
    when (state) {
        NotificationsState.Empty -> item { EmptyState() }
        is NotificationsState.Success -> items(state.notifications, key = { it.id }) { item ->
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
    item: NotificationUI,
    onNotificationItemDelete: (NotificationUI) -> Unit,
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
private fun NotificationDetails(item: NotificationUI, modifier: Modifier) {
    val context = LocalContext.current
    Column(modifier) {
        Text(text = item.title, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
        Text(text = item.body, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold))
        Text(text = item.date)
        item.url?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            Text(
                text = it,
                style = TextStyle(color = Color(0xFF0000EE)),
                modifier = Modifier.clickable { context.startActivity(intent) },
            )
        }
    }
}

@Composable
private fun TrashButton(
    item: NotificationUI,
    onNotificationItemDelete: (NotificationUI) -> Unit,
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


@Composable
fun TaskBar(
    dappIconUrl: String,
    dappName: String,
    onBackIconClick: () -> Unit,
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(30.dp))
    ) {
        val startGuideline = createGuidelineFromStart(10.dp)

        val (
            backIcon,
            icon,
            title,
        ) = createRefs()

        Icon(
            modifier = Modifier
                .constrainAs(backIcon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(startGuideline)
                    width = Dimension.value(12.dp)
                    height = Dimension.value(12.dp)
                }
                .clickable { onBackIconClick() },
            tint = Color(0xFF141414),
            imageVector = ImageVector.vectorResource(id = CommonR.drawable.chevron_left),
            contentDescription = "BackArrow",
        )

        AsyncImage(
            modifier = Modifier
                .constrainAs(icon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(backIcon.end, 8.dp)
                    width = Dimension.value(32.dp)
                    height = Dimension.value(32.dp)
                },
            model = ImageRequest.Builder(LocalContext.current)
                .data(dappIconUrl)
                .scale(Scale.FILL)
                .crossfade(true)
                .placeholder(R.drawable.green_check)
                .build(),
            contentDescription = null,
        )

        Text(
            modifier = Modifier
                .constrainAs(title) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(icon.end, 8.dp)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                    verticalChainWeight = .5f
                },
            text = dappName,
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 23.4.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFF141414),
            )
        )
    }
}

@Composable
fun Notification(isUnread: Boolean = false, notificationUI: NotificationUI) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
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
                color = Color(0xFF141414),
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
                color = Color(0xFF9EA9A9),
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
                color = Color(0xFF585F5F),
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
            onBackClick = {}
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
