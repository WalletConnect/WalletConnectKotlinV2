@file:OptIn(ExperimentalFoundationApi::class)

package com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox

import android.widget.Toast
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
import androidx.navigation.NavHostController
import com.walletconnect.notify.client.InvalidDidJsonFileException
import com.walletconnect.sample.common.ui.WCTopAppBar
import com.walletconnect.sample.common.ui.theme.PreviewTheme
import com.walletconnect.sample.common.ui.theme.UiModePreview
import com.walletconnect.sample.common.ui.theme.blue_accent
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.ui.common.ImageUrl
import com.walletconnect.sample.wallet.ui.common.subscriptions.ActiveSubscriptionsUI
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.discover.DiscoverTab
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.discover.ExplorerApp
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.subscriptions.SubscriptionsTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.URISyntaxException


@Composable
fun InboxRoute(navController: NavHostController, viewModel: InboxViewModel) {

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
        viewModel::unsubscribeFromDapp,
        viewModel::subscribeToDapp,
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
    onSubscribedClick: (explorerApp: ExplorerApp, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) -> Unit,
    onSubscribeClick: (explorerApp: ExplorerApp, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) -> Unit,
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
            onSubscribedClick,
            onSubscribeClick,
            activeSubscriptions,
            apps,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabViewWithPager(
    navController: NavHostController,
    subscriptionsState: SubscriptionsState,
    discoverState: DiscoverState,
    coroutineScope: CoroutineScope,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onRetry: () -> Unit,
    onSubscribedClick: (explorerApp: ExplorerApp, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) -> Unit,
    onSubscribeClick: (explorerApp: ExplorerApp, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) -> Unit,
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
            val onFailure: (Throwable) -> Unit = { error: Throwable ->
                val message = when (error) {
                    is InvalidDidJsonFileException -> "Wrong dapp config. Failed to parse did.json file"
                    is URISyntaxException -> "Wrong dapp config. Unable to open website. Reason: ${error.reason}"
                    else -> error.localizedMessage
                }
                coroutineScope.launch {
                    Toast.makeText(navController.context, message, Toast.LENGTH_SHORT).show()
                }
            }

            when (page) {
                0 -> SubscriptionsTab(navController, subscriptionsState, activeSubscriptions) { navigate(1) }
                1 -> DiscoverTab(
                    discoverState, apps,
                    onSubscribedClick = { explorerApp -> onSubscribedClick(explorerApp, { }, onFailure) },
                    onSubscribeClick = { explorerApp -> onSubscribeClick(explorerApp, { }, onFailure) },
                    onRetry = onRetry, onFailure = onFailure
                )

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
            NavHostController(LocalContext.current),
            state,
            DiscoverState.Fetched,
            CoroutineScope(SupervisorJob() + Dispatchers.IO),
            "",
            {},
            {},
            { _, _, _ -> },
            { _, _, _ -> },
            emptyList(),
            emptyList()
        )
    }
}

@Composable
@UiModePreview
fun InboxScreenSuccessPreview(@PreviewParameter(InboxScreenActiveSubscriptionsPreviewProvider::class) activeSubscriptions: List<ActiveSubscriptionsUI>) {
    PreviewTheme {
        InboxScreen(
            NavHostController(LocalContext.current),
            SubscriptionsState.Success,
            DiscoverState.Fetched,
            CoroutineScope(SupervisorJob() + Dispatchers.IO),
            "",
            {},
            {},
            { _, _, _ -> },
            { _, _, _ -> },
            emptyList(),
            emptyList()
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
                ImageUrl("https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Icon/Gradient/Icon.png", "", ""),
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