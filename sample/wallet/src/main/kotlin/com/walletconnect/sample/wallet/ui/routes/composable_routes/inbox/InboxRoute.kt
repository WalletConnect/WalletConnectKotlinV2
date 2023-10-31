@file:OptIn(ExperimentalFoundationApi::class)

package com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.walletconnect.notify.client.InvalidDidJsonFileException
import com.walletconnect.sample.common.ui.WCTopAppBar
import com.walletconnect.sample.common.ui.theme.PreviewTheme
import com.walletconnect.sample.common.ui.theme.UiModePreview
import com.walletconnect.sample.common.ui.theme.blue_accent
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.ui.common.subscriptions.ActiveSubscriptionsUI
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.discover.DiscoverTab
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.discover.ExplorerApp
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.subscriptions.SubscriptionsTab
import com.walletconnect.sample.wallet.ui.routes.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.URISyntaxException


@Composable
fun InboxRoute(navController: NavHostController) {
    val viewModel: InboxViewModel = viewModel()

    val subscriptionsState by viewModel.subscriptionsState.collectAsState(SubscriptionsState.Searching)
    val searchText by viewModel.searchText.collectAsState()
    val activeSubscriptions by viewModel.activeSubscriptions.collectAsState()
    val discoverState by viewModel.discoverState.collectAsState()
    val apps by viewModel.explorerApps.collectAsState()
    val scope = rememberCoroutineScope()

    InboxScreen(
        navController,
        subscriptionsState,
        discoverState,
        scope,
        searchText,
        viewModel::onSearchTextChange,
        viewModel::retryFetchingExplorerApps,
        activeSubscriptions,
        apps,
    )
}

@Composable
private fun InboxScreen(
    navController: NavHostController,
    subscriptionsState: SubscriptionsState,
    discoverState: DiscoverState,
    coroutineScope: CoroutineScope,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onRetry: () -> Unit,
    activeSubscriptions: List<ActiveSubscriptionsUI>,
    apps: List<ExplorerApp>,
) {
    Column(modifier = Modifier.fillMaxHeight()) {
        WCTopAppBar(titleText = "Inbox")
        TabViewWithPager(
            navController,
            subscriptionsState,
            discoverState,
            coroutineScope,
            searchText,
            onSearchTextChange,
            onRetry,
            activeSubscriptions,
            apps,
        )
    }
}

@Composable
fun TabViewWithPager(
    navController: NavHostController,
    subscriptionsState: SubscriptionsState,
    discoverState: DiscoverState,
    coroutineScope: CoroutineScope,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onRetry: () -> Unit,
    activeSubscriptions: List<ActiveSubscriptionsUI>,
    apps: List<ExplorerApp>,
) {
    val tabTitles = listOf("Subscriptions", "Discover")
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabTitles.size })

    val navigate = { index: Int ->
        coroutineScope.launch {
            pagerState.animateScrollToPage(index)
        }
    }

    Column {
        TabRow(
            backgroundColor = MaterialTheme.colors.background,
            selectedTabIndex = pagerState.currentPage,
            indicator = @Composable { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = blue_accent
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    modifier = Modifier.fillMaxWidth(),
                    text = { Text(title, textAlign = TextAlign.Start) },
                    selected = pagerState.currentPage == index,
                    onClick = { navigate(index) }
                )
            }
        }

        SearchBar(searchText, onSearchTextChange)
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> SubscriptionsTab(navController, subscriptionsState, activeSubscriptions) { navigate(1) }
                1 -> DiscoverTab(discoverState, apps, onSubscribeSuccess = { navigate(0) }, onRetry = onRetry, onFailure = {
                    val message = when (it) {
                        is InvalidDidJsonFileException -> "Wrong dapp config. Failed to parse did.json file"
                        is URISyntaxException -> "Wrong dapp config. Unable to open website. Reason: ${it.reason}"
                        else -> it.localizedMessage
                    }
                    coroutineScope.launch {
                        navController.showSnackbar(message)
                    }

                })

                else -> throw IllegalArgumentException("Invalid page index: $page")
            }
        }
    }
}

@Composable
fun SearchBar(searchText: String, onSearchTextChange: (String) -> Unit) {
    val customTextSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colors.onBackground,
        backgroundColor = MaterialTheme.colors.onBackground.copy(alpha = 0.2f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        OutlinedTextField(
            value = searchText,
            maxLines = 1,
            onValueChange = onSearchTextChange,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                cursorColor = MaterialTheme.colors.onBackground,
                focusedBorderColor = MaterialTheme.colors.onBackground,
                focusedLabelColor = MaterialTheme.colors.onBackground
            ),
            leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_magnifying_glass), contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 10.dp),
            placeholder = { Text(text = "Find you favourite app") }
        )
    }
}


@Composable
@UiModePreview
fun InboxScreenSearchingAndFailureStatePreview(@PreviewParameter(InboxScreenStatePreviewProvider::class) state: SubscriptionsState) {
    PreviewTheme {
        InboxScreen(
            NavHostController(LocalContext.current), state, DiscoverState.Success, CoroutineScope(SupervisorJob() + Dispatchers.IO), "", {}, {}, emptyList(), emptyList()

        )
    }
}

@Composable
@UiModePreview
fun InboxScreenSuccessPreview(@PreviewParameter(InboxScreenActiveSubscriptionsPreviewProvider::class) activeSubscriptions: List<ActiveSubscriptionsUI>) {
    PreviewTheme {
        InboxScreen(
            NavHostController(LocalContext.current), SubscriptionsState.Success, DiscoverState.Success, CoroutineScope(SupervisorJob() + Dispatchers.IO), "", {}, {}, emptyList(), emptyList()
        )
    }
}

@Composable
@UiModePreview
fun InboxItemPreview() {
    PreviewTheme {
//        todo add previews
//        ActiveSubscriptionItem(NavHostController(LocalContext.current), "", "", 0, "")
    }
}

private class InboxScreenStatePreviewProvider : PreviewParameterProvider<SubscriptionsState> {
    override val values: Sequence<SubscriptionsState> = sequenceOf(
        SubscriptionsState.Searching,
        SubscriptionsState.Failure(Throwable("This is a test error")),
    )
}

private class InboxScreenActiveSubscriptionsPreviewProvider : PreviewParameterProvider<List<ActiveSubscriptionsUI>> {
    override val values: Sequence<List<ActiveSubscriptionsUI>> = sequenceOf(
        emptyList(),
        listOf(
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
        )
    )
}