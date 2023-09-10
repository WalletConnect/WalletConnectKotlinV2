package com.walletconnect.web3.modal.ui.routes.connect.all_wallets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.modal.utils.isLandscape
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.ContentDescription
import com.walletconnect.web3.modal.ui.components.internal.commons.HorizontalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.ScanQRIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.WalletsLazyGridView
import com.walletconnect.web3.modal.ui.components.internal.commons.inputs.SearchInput
import com.walletconnect.web3.modal.ui.components.internal.commons.walletsGridItems
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.previews.testWallets
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import kotlinx.coroutines.launch

@Composable
internal fun AllWalletsRoute(
    navController: NavController,
    wallets: List<Wallet>
) {
    AllWalletsContent(
        wallets = wallets,
        onWalletItemClick = { navController.navigate(Route.REDIRECT.path + "/${it.id}&${it.name}") },
        onScanQRClick = { navController.navigate(Route.QR_CODE.path) }
    )
}

@Composable
private fun AllWalletsContent(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit,
    onScanQRClick: () -> Unit
) {
    var searchInputValue by rememberSaveable() { mutableStateOf("") }
    var searchedWallets = wallets.filteredWallets(searchInputValue)
    val gridFraction = if (isLandscape) 1f else .9f
    val color = Web3ModalTheme.colors.background.color275
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxHeight(gridFraction)
            .padding(horizontal = 12.dp),
    ) {
        VerticalSpacer(height = 12.dp)
        Row {
            SearchInput(
                searchValue = searchInputValue,
                modifier = Modifier.weight(1f),
                onSearchValueChange = { searchInputValue = it },
                onClearClick = {
                    searchedWallets = wallets
                    coroutineScope.launch {
                        gridState.scrollToItem(0)
                    }
                }
            )
            HorizontalSpacer(width = 12.dp)
            ScanQRIcon(onClick = onScanQRClick)
        }
        if (searchedWallets.isEmpty()) {
            NoWalletsFoundItem()
        } else {
            WalletsLazyGridView(
                modifier = Modifier
                    .padding(top = 12.dp)
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
                .background(Web3ModalTheme.colors.overlay05, RoundedCornerShape(12.dp))
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
