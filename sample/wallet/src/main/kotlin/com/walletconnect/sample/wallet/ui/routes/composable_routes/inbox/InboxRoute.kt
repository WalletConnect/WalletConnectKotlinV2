package com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
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
import com.walletconnect.sample.common.ui.WCTopAppBar
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.ui.routes.Route
import timber.log.Timber
import java.net.URLEncoder


@Composable
fun InboxRoute(navController: NavHostController) {
    val viewModel: InboxViewModel = viewModel()
    val state by viewModel.state.collectAsState(InboxState.Empty)
    Timber.d("InboxScreenRoute state - $state")

    InboxScreen(navController, state = state) { navController.popBackStack() }
}

@Composable
private fun InboxScreen(
    navController: NavHostController,
    state: InboxState,
    onBackClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxHeight()) {
        WCTopAppBar(titleText = "Inbox")

        when (state) {
            is InboxState.Empty -> {
            }

            is InboxState.Subscriptions -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.Top)
                ) {
                    items(
                        state.listOfActiveSubscriptions
                    ) { subscription ->
                        InboxItem(navController, subscription.icon, subscription.name, subscription.messageCount, subscription.topic)
                    }
                }
            }
        }
    }
}

@Composable
fun InboxItem(navController: NavHostController, url: String, name: String, messageCount: Int, topic: String) {
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        contentPadding = PaddingValues(10.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface),
        border = BorderStroke(0.dp, color = Color.Transparent),
        onClick = {
            navController.navigate("${Route.Notifications.path}/$topic/$name/${URLEncoder.encode(url, "UTF-8")}")
        }
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color = Color.Transparent)
        ) {
            val (
                icon,
                dappName,
                badge,
            ) = createRefs()

            AsyncImage(
                modifier = Modifier
                    .constrainAs(icon) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(dappName.start)
                        width = Dimension.value(40.dp)
                        height = Dimension.matchParent
                    },
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .scale(Scale.FILL)
                    .crossfade(true)
                    .placeholder(R.drawable.sad_face)
                    .build(),
                contentDescription = null,
            )

            Text(
                modifier = Modifier
                    .constrainAs(dappName) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(icon.end, 10.dp)
                        end.linkTo(badge.start, 5.dp)
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                        verticalChainWeight = .5f
                        horizontalChainWeight = 0f
                    },
                text = name,
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight(700),
                    color = MaterialTheme.colors.onSurface,
                    textAlign = TextAlign.Center,
                )
            )

            Box(
                modifier = Modifier
                    .constrainAs(badge) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(dappName.end)
                        end.linkTo(parent.end)
                        width = Dimension.value(22.dp)
                        height = Dimension.value(22.dp)
                    }
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
}

@Composable
@Preview
fun InboxItemPreview() {
    InboxItem(NavHostController(LocalContext.current), "", "", 0, "")
}

@Composable
@Preview
fun InboxScreenPreview(@PreviewParameter(InboxScreenPreviewProvider::class) state: InboxState) {
    InboxScreen(NavHostController(LocalContext.current), state) {}
}

private class InboxScreenPreviewProvider : PreviewParameterProvider<InboxState> {
    override val values: Sequence<InboxState> = sequenceOf(
        InboxState.Empty,
        InboxState.Subscriptions(
            listOf(
                InboxState.Subscriptions.ActiveSubscriptions("", "", "Dapp Name", 0)
            )
        )
    )
}