package com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.subscriptions


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.walletconnect.sample.wallet.ui.common.ImageUrl
import com.walletconnect.sample.wallet.ui.common.subscriptions.ActiveSubscriptionsUI
import com.walletconnect.sample.wallet.ui.common.subscriptions.SubscriptionIcon
import com.walletconnect.sample.wallet.ui.routes.Route
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.LazyColumnSurroundedWithFogVertically


@Composable
fun ActiveSubscriptions(navController: NavHostController, activeSubscriptions: List<ActiveSubscriptionsUI>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (activeSubscriptions.isEmpty()) {
            Text(text = "No apps found")
        } else {
            LazyColumnSurroundedWithFogVertically(indexByWhichShouldDisplayBottomFog = activeSubscriptions.lastIndex - 5) {
                unreadActiveSubscriptionItems(navController, activeSubscriptions)
                readActiveSubscriptionItems(navController, activeSubscriptions)
            }
        }
    }

}

fun LazyListScope.unreadActiveSubscriptionItems(navController: NavHostController, activeSubscriptions: List<ActiveSubscriptionsUI>) {
    val unreadActiveSubscriptionItems = activeSubscriptions.filter { it.hasUnreadMessages }

    if (unreadActiveSubscriptionItems.isNotEmpty()) {
        item {
            Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = "UNREAD",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight(700),
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.3f)
                    )
                )
            }
        }
        items(unreadActiveSubscriptionItems) { subscription ->
            Spacer(modifier = Modifier.height(8.dp))
            UnreadActiveSubscriptionItem(navController, subscription.imageUrl, subscription.name, subscription.messageCount, subscription.description, subscription.topic)
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


fun LazyListScope.readActiveSubscriptionItems(navController: NavHostController, activeSubscriptions: List<ActiveSubscriptionsUI>) {
    val readActiveSubscriptionItems = activeSubscriptions.filter { !it.hasUnreadMessages }

    if (readActiveSubscriptionItems.isNotEmpty()) {
        item {
            Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    text = "SUBSCRIBED",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight(700),
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.3f)
                    )
                )
            }
        }
        items(readActiveSubscriptionItems) { subscription ->
            Spacer(modifier = Modifier.height(8.dp))
            ReadActiveSubscriptionItem(navController, subscription.imageUrl, subscription.name, subscription.lastReceived, subscription.description, subscription.topic)
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


@Composable
fun ActiveSubscriptionItem(navController: NavHostController, imageUrl: ImageUrl, name: String, description: String, topic: String, endContent: @Composable (() -> Unit)) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { navController.navigate("${Route.Notifications.path}/$topic") }
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubscriptionIcon(size = 48.dp, imageUrl = imageUrl)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(
                    text = name,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight(500),
                    )
                )
                Text(
                    text = description,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight(400),
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            endContent()
        }
    }
}

@Composable
fun ReadActiveSubscriptionItem(navController: NavHostController, imageUrl: ImageUrl, name: String, lastReceived: String, description: String, topic: String) {
    ActiveSubscriptionItem(navController, imageUrl, name, description, topic) {
        Text(
            text = lastReceived,
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight(510),
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.3f)
            )
        )
    }
}


@Composable
fun UnreadActiveSubscriptionItem(navController: NavHostController, imageUrl: ImageUrl, name: String, messageCount: Int, description: String, topic: String) {
    ActiveSubscriptionItem(navController, imageUrl, name, description, topic) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(width = 1.dp, color = Color(0x1A062B2B), shape = RoundedCornerShape(size = 20.dp))
                .background(color = Color(0xFF3396FF), shape = RoundedCornerShape(size = 20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(0.dp),
                text = if (messageCount > 99) "99+" else messageCount.toString(),
                style = TextStyle(
                    fontSize = 11.sp,
                    lineHeight = 11.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFFFFFFFF),
                    textAlign = TextAlign.Center,
                )
            )
        }
    }
}
