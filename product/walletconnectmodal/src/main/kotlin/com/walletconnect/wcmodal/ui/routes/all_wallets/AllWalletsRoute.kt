package com.walletconnect.wcmodal.ui.routes.all_wallets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.modal.ui.model.LoadingState
import com.walletconnect.modal.ui.model.search.SearchState
import com.walletconnect.wcmodal.ui.components.ModalSearchTopBar
import com.walletconnect.wcmodal.ui.components.walletsGridItems
import com.walletconnect.wcmodal.ui.preview.ModalPreview
import com.walletconnect.wcmodal.ui.theme.ModalTheme
import com.walletconnect.modal.utils.isLandscape
import com.walletconnect.wcmodal.domain.dataStore.WalletsData
import com.walletconnect.wcmodal.ui.WalletConnectModalViewModel
import com.walletconnect.wcmodal.ui.navigation.Route
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@Composable
internal fun AllWalletsRoute(
    navController: NavController,
    viewModel: WalletConnectModalViewModel
) {
    val walletsState by viewModel.walletsState.collectAsState()

    AllWalletsContent(
        walletsData = walletsState,
        searchPhrase = viewModel.searchPhrase,
        onSearch = { viewModel.search(it) },
        onSearchClear = { viewModel.clearSearch() },
        onFetchNextPage = { viewModel.fetchMoreWallets() },
        onWalletItemClick = { navController.navigate(Route.OnHold.path + "/${it.id}") },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
private fun AllWalletsContent(
    walletsData: WalletsData,
    searchPhrase: String,
    onSearch: (String) -> Unit,
    onSearchClear: () -> Unit,
    onFetchNextPage: () -> Unit,
    onWalletItemClick: (Wallet) -> Unit,
    onBackClick: () -> Unit,
) {
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val scrollToFirstItem = { coroutineScope.launch { gridState.scrollToItem(0) } }
    val searchState = remember {
        SearchState(
            searchPhrase = searchPhrase,
            onSearchSubmit = { onSearch(it).also { scrollToFirstItem() } },
            onClearInput = { onSearchClear().also { scrollToFirstItem() } }
        )
    }
    val gridFraction = if (isLandscape) 1f else .95f

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex != 0 && !gridState.canScrollForward }
            .distinctUntilChanged()
            .filter { it }
            .collect { onFetchNextPage() }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight(gridFraction)
            .padding(horizontal = 4.dp),
    ) {
        ModalSearchTopBar(
            searchState = searchState,
            onBackPressed = onBackClick,
        )
        if (walletsData.loadingState == LoadingState.REFRESH) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    strokeWidth = 4.dp,
                    color = ModalTheme.colors.main,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else if (walletsData.wallets.isEmpty()) {
            NoWalletsFoundItem()
        } else {
            WalletsGrid(gridState, walletsData, onWalletItemClick)
        }
    }
}

@Composable
fun NoWalletsFoundItem() {
    Text(
        text = "No wallets found",
        style = TextStyle(color = ModalTheme.colors.secondaryTextColor, fontSize = 16.sp),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 50.dp)
    )
}

@Composable
private fun WalletsGrid(
    gridState: LazyGridState,
    walletsData: WalletsData,
    onWalletItemClick: (Wallet) -> Unit
) {
    val color = ModalTheme.colors.background
    Box {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.FixedSize(82.dp),
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .graphicsLayer { alpha = 0.99f }
                .drawWithContent {
                    val colors = listOf(Color.Transparent, color)
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(colors, startY = 0f, endY = 40f),
                        blendMode = BlendMode.DstIn,
                    )
                },
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            walletsGridItems(walletsData.wallets, onWalletItemClick)
            if (walletsData.loadingState == LoadingState.APPEND) {
                loadingWalletsItems()
            }
        }
    }
}

private fun LazyGridScope.loadingWalletsItems() {
    items(10) {
        Surface(
            modifier = Modifier.padding(4.dp),
            color = Color.Transparent,
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(76.dp)
                    .height(96.dp)
                    .background(Color.Transparent)
                    .border(width = 1.dp, color = ModalTheme.colors.secondaryBackgroundColor, shape = RoundedCornerShape(16.dp))
            )
        }
    }
}


@Preview
@Composable
private fun AllWalletsPreview() {
    val walletsData = WalletsData.empty()
    ModalPreview {
        AllWalletsContent(walletsData, "", {}, {}, {}, {}, {})
    }
}
