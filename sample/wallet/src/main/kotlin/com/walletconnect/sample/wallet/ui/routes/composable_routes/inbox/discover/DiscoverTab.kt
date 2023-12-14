package com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.discover


import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walletconnect.sample.common.ui.theme.blue_accent
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.ui.common.subscriptions.SubscriptionIcon
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.DiscoverState
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.LazyColumnSurroundedWithFogVertically
import java.net.URI


@Composable
fun DiscoverTab(
    state: DiscoverState,
    apps: List<ExplorerApp>,
    onSubscribedClick: (app: ExplorerApp) -> Unit,
    onSubscribeClick: (app: ExplorerApp) -> Unit,
    onRetry: () -> Unit,
    onFailure: (Throwable) -> Unit,
) {
    when (state) {
        DiscoverState.Searching, DiscoverState.Fetching -> {
            EmptyOrLoadingOrFailureState(text = "Querying apps...") {
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(modifier = Modifier.size(80.dp), color = blue_accent)
            }
        }

        is DiscoverState.Failure -> {
            EmptyOrLoadingOrFailureState(text = "Failure querying apps from Explorer") {
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background),
                    onClick = {
                        onRetry()
                    }
                ) {
                    Text(
                        text = "Retry",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight(600),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colors.onBackground
                        )
                    )
                }
            }
        }

        is DiscoverState.Fetched -> {
            SuccessState(apps, onSubscribedClick, onSubscribeClick, onFailure)
        }

        is DiscoverState.Subscribing -> {
            EmptyOrLoadingOrFailureState(text = "Subscribing to ${state.explorerApp.name}") {
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(modifier = Modifier.size(80.dp), color = blue_accent)
            }
        }

        is DiscoverState.Unsubscribing -> {
            EmptyOrLoadingOrFailureState(text = "Unsubscribing from ${state.explorerApp.name}") {
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(modifier = Modifier.size(80.dp), color = blue_accent)
            }
        }
    }
}


@Composable
private fun SuccessState(apps: List<ExplorerApp>, onSubscribedClick: (app: ExplorerApp) -> Unit, onSubscribeClick: (app: ExplorerApp) -> Unit, onFailure: (Throwable) -> Unit) {
    Box() {
        if (apps.isNotEmpty()) {
            LazyColumnSurroundedWithFogVertically(indexByWhichShouldDisplayBottomFog = apps.lastIndex - 2) {
                itemsIndexed(apps) { index, app ->
                    ExplorerAppItem(explorerApp = app, onSubscribedClick, onSubscribeClick, onFailure)
                    if (index != apps.lastIndex) Spacer(modifier = Modifier.height(20.dp))
                }
            }
        } else {
            EmptyOrLoadingOrFailureState(text = "No apps found")
        }
    }
}


@Composable
private fun EmptyOrLoadingOrFailureState(text: String, content: @Composable ColumnScope.() -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = text)
            content()
        }
    }
}

@Composable
fun ExplorerAppItem(explorerApp: ExplorerApp, onSubscribedClick: (app: ExplorerApp) -> Unit, onSubscribeClick: (app: ExplorerApp) -> Unit, onFailure: (Throwable) -> Unit) {
    val context = LocalContext.current
    val gradientRadius = remember { mutableStateOf(IntSize(1, 1)) }
    val boxShape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .onGloballyPositioned { coordinates ->
                gradientRadius.value = coordinates.size
            }
            .height(180.dp)
            .clip(boxShape)
            .border(1.dp, ButtonDefaults.outlinedBorder.brush, boxShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0x403ABDF2), Color(0x203ABDF2), Color(0x103ABDF2), Color(0x053ABDF2), Color(0x00FFFFFF)),
                    center = Offset(gradientRadius.value.width.toFloat() / 6f, -(gradientRadius.value.height.toFloat() / 2.5f)),
                    radius = gradientRadius.value.width.toFloat()
                ), shape = boxShape
            )
            .clickable {
                runCatching {
                    val parsedUrl = Uri.parse(
                        URI(explorerApp.homepage)
                            .toURL()
                            .toString()
                    )
                    if (parsedUrl != null) {
                        val intent = Intent(Intent.ACTION_VIEW, parsedUrl)
                        context.startActivity(intent)
                    }
                }.getOrElse { onFailure(it) }
            }
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Column() {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SubscriptionIcon(size = 48.dp, imageUrl = explorerApp.imageUrl)

                if (explorerApp.isSubscribed) {
                    OutlinedButton(
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background),
                        onClick = { onSubscribedClick(explorerApp) }
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
                } else {
                    OutlinedButton(
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background),
                        onClick = { onSubscribeClick(explorerApp) }
                    ) {
                        Text(
                            text = "Subscribe",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight(600),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colors.onBackground
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = explorerApp.name,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight(600),
                    color = MaterialTheme.colors.onBackground
                )
            )
            Text(
                text = explorerApp.dappUrl.removePrefix("https://"),
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = explorerApp.description,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight(400),
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f)
                ),
                maxLines = 2
            )
        }
    }
}



