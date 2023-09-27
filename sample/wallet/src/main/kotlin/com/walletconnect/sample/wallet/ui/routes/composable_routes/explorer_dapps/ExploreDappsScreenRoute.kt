package com.walletconnect.sample.wallet.ui.routes.composable_routes.explorer_dapps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
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
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.common.ui.WCTopAppBar2
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.domain.EthAccountDelegate
import com.walletconnect.sample.wallet.domain.toEthAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun ExploreDappsScreenRoute(navController: NavHostController) {
    val viewModel: ExploreDappsViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val apps by viewModel.projects.collectAsState()

    SubscribedDappsScreen(
        state = state,
        navController = navController,
        onBackClick = {
            viewModel.viewModelScope.launch(Dispatchers.Main) {
                navController.popBackStack()
            }
        },
        apps,
        searchText,
        onFilterChange = { filter -> viewModel.onSearchTextChange(filter) },
    )
}


@Composable
private fun SubscribedDappsScreen(
    state: ExplorerState,
    navController: NavHostController,
    onBackClick: () -> Unit,
    apps: List<ExplorerApp>,
    filter: String,
    onFilterChange: (String) -> Unit,
) {

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        WCTopAppBar2(titleText = "Explore Apps", onBackIconClick = onBackClick)
        when (state) {
            ExplorerState.Searching -> {
                EmptyOrLoadingOrFailureState(text = "Querying apps...", showProgressBar = true)
            }

            is ExplorerState.Failure -> {
                EmptyOrLoadingOrFailureState(text = "Failure querying apps from Explorer")
            }

            is ExplorerState.Success -> {
                SuccessState(filter, apps, onBackClick, onFilterChange)
            }
        }
    }
}

@Composable
private fun SuccessState(filter: String, apps: List<ExplorerApp>, onBackClick: () -> Unit, onFilterChange: (String) -> Unit) {

    Column(modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 0.dp)) {
        OutlinedTextField(
            value = filter,
            onValueChange = onFilterChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Search") }
        )
        Spacer(modifier = Modifier.height(20.dp))
        if (apps.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.Top),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(apps) { app ->
                    DappCard(explorerApp = app, onBackClick)
                }
            }
        } else {
            EmptyOrLoadingOrFailureState(text = "No apps found")
        }
    }
}


@Composable
private fun EmptyOrLoadingOrFailureState(text: String, showProgressBar: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = text)
            if (showProgressBar) {
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(modifier = Modifier.size(80.dp))
            }
        }
    }
}

@Composable
private fun DappCard(explorerApp: ExplorerApp, onBackClick: () -> Unit) {
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .defaultMinSize(minHeight = 198.dp),
        elevation = ButtonDefaults.elevation(4.dp),
        colors = ButtonDefaults.outlinedButtonColors(backgroundColor = MaterialTheme.colors.surface),
        contentPadding = PaddingValues(all = 0.dp),
        shape = RoundedCornerShape(30.dp),
        onClick = {
            val subscribeParams = Notify.Params.Subscribe(explorerApp.homepage.toUri(), with(EthAccountDelegate) { account.toEthAddress() })
            NotifyClient.subscribe(
                params = subscribeParams,
                onSuccess = { onBackClick() },
                onError = { Timber.e(it.throwable) }
            )

        }) {
        ConstraintLayout(
            modifier = Modifier
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
                    .placeholder(R.drawable.sad_face)
                    .data(explorerApp.imageUrl.sm)
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
                text = explorerApp.name,
                style = TextStyle(
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight(700),
                    color = MaterialTheme.colors.onSurface,
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
                text = explorerApp.description,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colors.onSurface,
                )
            )

            Text(
                modifier = Modifier
                    .constrainAs(dappUrl) {
                        top.linkTo(dappDesc.bottom, 4.dp)
                        start.linkTo(startGuideline)
                        bottom.linkTo(bottomGuideline)
                    },
                text = explorerApp.dappUrl,
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colors.onSurface,
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
private fun DappCardPreview(@PreviewParameter(ExplorerDappPreviewProvider::class) dapp: ExplorerApp) {
    DappCard(explorerApp = dapp, {})
}

private class ExplorerDappPreviewProvider : PreviewParameterProvider<ExplorerApp> {
    override val values = sequenceOf(exampleApps[0], exampleApps[1])
}

@Composable
@Preview
fun SubscribedDappsScreenPreview(@PreviewParameter(SubscribedDappsScreenPreviewProvider::class) state: ExplorerState) {
    SubscribedDappsScreen(state, NavHostController(LocalContext.current), {}, exampleApps, "", {})
}

private class SubscribedDappsScreenPreviewProvider : PreviewParameterProvider<ExplorerState> {
    override val values: Sequence<ExplorerState> = sequenceOf(
        ExplorerState.Success,
        ExplorerState.Searching,
        ExplorerState.Failure(Throwable("Timeout")),
    )
}

private val exampleApps = listOf(
    ExplorerApp(
        imageUrl = ImageUrl(
            "https://cryptologos.cc/logos/uniswap-uni-logo.png?v=026",
            "https://cryptologos.cc/logos/uniswap-uni-logo.png?v=026",
            "https://cryptologos.cc/logos/uniswap-uni-logo.png?v=026"
        ),
        name = "Test Dapp",
        description = "Test Dapp Description",
        homepage = "https://test.dapp",
        id = "",
        imageId = "",
        dappUrl = "test.dapp"
    ),
    ExplorerApp(
        imageUrl = ImageUrl(
            "https://cryptologos.cc/logos/uniswap-uni-logo.png?v=026",
            "https://cryptologos.cc/logos/uniswap-uni-logo.png?v=026",
            "https://cryptologos.cc/logos/uniswap-uni-logo.png?v=026"
        ),
        name = "Test Dapp",
        description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
        homepage = "https://test.dapp",
        id = "",
        imageId = "",
        dappUrl = "test.dapp"
    )
)