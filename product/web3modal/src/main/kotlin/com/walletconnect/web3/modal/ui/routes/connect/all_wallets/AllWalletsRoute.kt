package com.walletconnect.web3.modal.ui.routes.connect.all_wallets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.modal.utils.isLandscape
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.ContentDescription
import com.walletconnect.web3.modal.ui.components.internal.commons.HorizontalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.ScanQRIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.inputs.SearchInput
import com.walletconnect.web3.modal.ui.components.internal.commons.inputs.SearchState
import com.walletconnect.web3.modal.ui.components.internal.commons.inputs.SearchStatePreviewProvider
import com.walletconnect.web3.modal.ui.components.internal.commons.walletsGridItems
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.previews.testWallets
import com.walletconnect.web3.modal.ui.routes.connect.ConnectState
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import com.walletconnect.web3.modal.ui.utils.conditionalModifier
import kotlinx.coroutines.launch

@Composable
internal fun AllWalletsRoute(
    connectState: ConnectState
) {
    AllWalletsContent(
        wallets = connectState.wallets,
        onWalletItemClick = { wallet -> connectState.navigateToRedirectRoute(wallet) },
        onScanQRClick = { connectState.navigateToScanQRCode() }
    )
}

@Composable
private fun AllWalletsContent(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit,
    onScanQRClick: () -> Unit
) {
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val searchState = remember { SearchState(onClearInput = { coroutineScope.launch { gridState.scrollToItem(0) } }) }
    val state by searchState.state.collectAsState()
    val searchedWallets = wallets.filteredWallets(state.searchValue)
    val gridFraction = if (isLandscape) 1f else .95f
    val color = Web3ModalTheme.colors.background.color275

    Column(modifier = Modifier.fillMaxHeight(gridFraction)) {
        SearchInputRow(searchState, onScanQRClick)
        if (searchedWallets.isEmpty()) {
            NoWalletsFoundItem()
        } else {
            LazyVerticalGrid(
                state = rememberLazyGridState(),
                columns = GridCells.Adaptive(76.dp),
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp)
                    .graphicsLayer { alpha = 0.99f }
                    .drawWithContent {
                        val colors = listOf(Color.Transparent, color)
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(colors, startY = 0f, endY = 40f),
                            blendMode = BlendMode.DstIn,
                        )
                    }
            ) {
                walletsGridItems(searchedWallets, onWalletItemClick)
            }
        }
    }
}

@Composable
private fun SearchInputRow(
    searchState: SearchState,
    onScanQRClick: () -> Unit
) {
    val defaultSpacing: Dp = 12.dp
    val focusBorderWidth: Dp = 4.dp
    val focusedSpacing: Dp = defaultSpacing - focusBorderWidth
    val focusBorderColor = Web3ModalTheme.colors.accent20

    val paddingValues: PaddingValues
    val spacerValue: Dp
    if (searchState.isFocused) {
        spacerValue = focusedSpacing
        paddingValues = PaddingValues(start = focusedSpacing, top = focusedSpacing, bottom = focusedSpacing, end = defaultSpacing)
    } else {
        spacerValue = 12.dp
        paddingValues = PaddingValues(12.dp)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(paddingValues)
    ) {
        Box(modifier = Modifier
            .weight(1f)
            .conditionalModifier(searchState.isFocused) {
                border(width = focusBorderWidth, color = focusBorderColor, RoundedCornerShape(16.dp)).padding(focusBorderWidth)
            }) {
            SearchInput(searchState)
        }
        HorizontalSpacer(width = spacerValue)
        ScanQRIcon(onClick = onScanQRClick)
    }
}


@Composable
private fun ColumnScope.NoWalletsFoundItem() {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_wallet),
            contentDescription = ContentDescription.WALLET.description,
            modifier = Modifier
                .size(40.dp)
                .background(Web3ModalTheme.colors.grayGlass05, RoundedCornerShape(12.dp))
                .padding(7.dp)
        )
        VerticalSpacer(height = 20.dp)
        Text(
            text = "No Wallet found",
            style = TextStyle(color = Web3ModalTheme.colors.foreground.color125, fontSize = 16.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

}

private fun List<Wallet>.filteredWallets(value: String): List<Wallet> = this.filter { it.name.startsWith(prefix = value, ignoreCase = true) }

@UiModePreview
@Composable
private fun SearchRowPreview(
    @PreviewParameter(SearchStatePreviewProvider::class) state: SearchState
) {
    ComponentPreview { SearchInputRow(searchState = state, {}) }
}

@UiModePreview
@Composable
private fun AllWalletsEmptyPreview() {
    Web3ModalPreview {
        AllWalletsContent(listOf(), {}, {})
    }
}

@UiModePreview
@Composable
private fun AllWalletsPreview() {
    Web3ModalPreview {
        AllWalletsContent(testWallets, {}, {})
    }
}
