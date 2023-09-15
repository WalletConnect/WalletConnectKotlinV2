package com.walletconnect.sample.wallet.ui.routes.composable_routes.explorer_dapps

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.common.tag
import com.walletconnect.sample.common.ui.WCTopAppBar2
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.domain.EthAccountDelegate
import com.walletconnect.sample.wallet.domain.toEthAddress

@Composable
fun ExploreDappsScreenRoute(navController: NavHostController) {
    val viewModel: ExploreDappsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    SubscribedDappsScreen(
        state = state,
        navController = navController,
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
private fun SubscribedDappsScreen(
    state: ExplorerDappsState,
    navController: NavHostController,
    onBackClick: () -> Unit,
) {
    Column {
        WCTopAppBar2(
            titleText = "Dapps",
            clickableIcon = R.drawable.ic_search,
            onIconClick = {
                val subscribeParams = Notify.Params.Subscribe("https://dev.gm.walletconnect.com".toUri(), with(EthAccountDelegate) { account.toEthAddress() })
                NotifyClient.subscribe(
                    params = subscribeParams,
                    onSuccess = {
                        Log.e(tag(this), "Subscribe Success")
                    },
                    onError = {
                        Log.e(tag(this), it.throwable.stackTraceToString())
                    }
                )
            },
            onBackIconClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.Top)
        ) {
            items(state.explorerDapps) { dapp ->
                DappCard(explorerDapp = dapp, navController = navController)
            }
        }
    }
}

@Composable
private fun DappCard(explorerDapp: ExplorerDapp, navController: NavHostController) {
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .defaultMinSize(minHeight = 198.dp),
        elevation = ButtonDefaults.elevation(4.dp),
        colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.White),
        contentPadding = PaddingValues(all = 0.dp),
        shape = RoundedCornerShape(30.dp),
        onClick = { /* subscribe and return back */ }) {
        ConstraintLayout(
            modifier = Modifier
                .background(color = Color.Transparent)
                .fillMaxWidth()
        ) {
            val topGuideline = createGuidelineFromTop(24.dp)
            val bottomGuideline = createGuidelineFromBottom(24.dp)
            val startGuideline = createGuidelineFromStart(28.dp)
            val endGuideline = createGuidelineFromEnd(28.dp)

            val (
                icon,
                dappName,
                dappDesc,
                dappUrl,
                externalLink,
            ) = createRefs()

            AsyncImage(
                modifier = Modifier
                    .shadow(elevation = 32.dp, spotColor = Color(0x1A000000), ambientColor = Color(0x1A000000))
                    .shadow(elevation = 14.dp, spotColor = Color(0x1F000000), ambientColor = Color(0x1F000000))
                    .constrainAs(icon) {
                        top.linkTo(topGuideline)
                        start.linkTo(startGuideline)
                        bottom.linkTo(dappName.top)
                        width = Dimension.value(60.dp)
                        height = Dimension.value(60.dp)
                    },
                model = ImageRequest.Builder(LocalContext.current)
                    .placeholder(R.drawable.green_check)
                    .data(explorerDapp.url)
                    .crossfade(true)
                    .build(),
                contentDescription = "",
            )

            Text(
                modifier = Modifier
                    .constrainAs(dappName) {
                        top.linkTo(icon.bottom, 12.dp)
                        start.linkTo(startGuideline)
                        bottom.linkTo(dappDesc.top)
                        width = Dimension.wrapContent
                        height = Dimension.wrapContent
                    },
                text = explorerDapp.name,
                style = TextStyle(
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight(700),
                    color = Color(0xFF141414),
                )
            )

            Text(
                modifier = Modifier
                    .constrainAs(dappDesc) {
                        top.linkTo(dappName.bottom, 4.dp)
                        start.linkTo(startGuideline)
                        bottom.linkTo(dappUrl.top)
                        end.linkTo(endGuideline)
                        horizontalChainWeight = 0f
                        width = Dimension.fillToConstraints
                        height = Dimension.wrapContent
                    },
                text = explorerDapp.desc,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF3B4040),
                )
            )

            Text(
                modifier = Modifier
                    .constrainAs(dappUrl) {
                        top.linkTo(dappDesc.bottom, 4.dp)
                        start.linkTo(startGuideline)
                        bottom.linkTo(bottomGuideline)
                    },
                text = explorerDapp.url,
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF798686),
                    letterSpacing = 0.12.sp,
                )
            )

            Image(
                modifier = Modifier
                    .constrainAs(externalLink) {
                        top.linkTo(topGuideline)
                        end.linkTo(endGuideline)
                    }
                    .size(12.dp),
                painter = painterResource(R.drawable.ic_external_link),
                contentDescription = ""
            )
        }
    }
}

@Composable
@Preview
private fun DappCardPreview(@PreviewParameter(ExplorerDappPreviewProvider::class) dapp: ExplorerDapp) {
    DappCard(explorerDapp = dapp, navController = NavHostController(LocalContext.current))
}

private class ExplorerDappPreviewProvider : PreviewParameterProvider<ExplorerDapp> {
    override val values = sequenceOf(
        ExplorerDapp(
            topic = "",
            icon = "https://cryptologos.cc/logos/uniswap-uni-logo.png?v=026",
            name = "Test Dapp",
            desc = "Test Dapp Description",
            url = "https://test.dapp"
        ),
        ExplorerDapp(
            topic = "",
            icon = "https://cryptologos.cc/logos/uniswap-uni-logo.png?v=026",
            name = "Test Dapp",
            desc = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            url = "https://test.dapp"
        )
    )
}

@Composable
@Preview
fun SubscribedDappsScreenPreview(@PreviewParameter(SubscribedDappsScreenPreviewProvider::class) state: ExplorerDappsState) {
    SubscribedDappsScreen(state, NavHostController(LocalContext.current)) {}
}

private class SubscribedDappsScreenPreviewProvider : PreviewParameterProvider<ExplorerDappsState> {
    override val values: Sequence<ExplorerDappsState> = sequenceOf(
        ExplorerDappsState(
            listOf(
                ExplorerDapp(
                    topic = "",
                    icon = "https://cryptologos.cc/logos/uniswap-uni-logo.png?v=026",
                    name = "Test Dapp",
                    desc = "Test Dapp Description",
                    url = "https://test.dapp"
                ),
                ExplorerDapp(
                    topic = "",
                    icon = "https://cryptologos.cc/logos/uniswap-uni-logo.png?v=026",
                    name = "Test Dapp",
                    desc = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                    url = "https://test.dapp"
                )
            )
        )
    )
}