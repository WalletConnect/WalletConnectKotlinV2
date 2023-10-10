package com.walletconnect.sample.wallet.ui.routes.composable_routes.notifications


import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.ui.common.subscriptions.ActiveSubscriptionsUI
import timber.log.Timber


@Composable
fun NotificationsHeader(
    currentSubscription: ActiveSubscriptionsUI,
    isMoreExpanded: Boolean,
    onBackIconClick: () -> Unit,
    onMoreIconClick: () -> Unit,
    onNotificationSettings: () -> Unit,
    onUnsubscribe: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                modifier = Modifier
                    .width(20.dp)
                    .height(28.dp)
                    .clickable(indication = rememberRipple(bounded = false, radius = 20.dp), interactionSource = remember { MutableInteractionSource() }, onClick = onBackIconClick)
                    .padding(vertical = 8.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_chevron_left),
                contentDescription = "Back arrow",
            )
            AsyncImage(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(1.dp, ButtonDefaults.outlinedBorder.brush, CircleShape),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(currentSubscription.icon)
                    .crossfade(200)
                    .build(),
                contentDescription = null
            )
            NotificationsOptionsMenu(
                onMoreIconClick = onMoreIconClick,
                expanded = isMoreExpanded,
                onDismissRequest = onMoreIconClick,
                onNotificationSettings = onNotificationSettings,
                onUnsubscribe = onUnsubscribe
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = currentSubscription.name,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight(700),
                )
            )
            if (currentSubscription.isVerified) {
                Spacer(modifier = Modifier.width(5.dp))
                Image(
                    modifier = Modifier
                        .size(18.dp)
                        .alpha(0.0f),
                    painter = painterResource(id = R.drawable.ic_verified_round),
                    contentDescription = null,
                )
            }
        }
        Text(
            text = currentSubscription.appDomain.removePrefix("https://"),
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight(510),
                color = MaterialTheme.colors.onBackground.copy(0.7f),
                textAlign = TextAlign.Center,
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier.fillMaxWidth(0.7f),
            text = currentSubscription.description,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight(400),
                textAlign = TextAlign.Center,
            ),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(disabledBackgroundColor = MaterialTheme.colors.background),
            onClick = {},
            enabled = false
        ) {
            val color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
            Text(
                text = "Subscribed",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight(600),
                    textAlign = TextAlign.Center,
                    color = color
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(painterResource(id = R.drawable.ic_check_mark), contentDescription = null, tint = color)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
    Divider()
}