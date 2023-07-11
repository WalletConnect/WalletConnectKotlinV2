package com.walletconnect.modal.ui.routes.connect_wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.modal.R
import com.walletconnect.modal.ui.components.ModalTopBar
import com.walletconnect.modal.ui.components.WalletImage
import com.walletconnect.modal.ui.components.WalletListItem
import com.walletconnect.modal.ui.components.WalletsLazyGridView
import com.walletconnect.modal.ui.components.walletsGridItems
import com.walletconnect.modal.ui.navigation.Route
import com.walletconnect.modal.ui.preview.ModalPreview
import com.walletconnect.modal.ui.theme.ModalTheme
import com.walletconnect.modalcore.utils.goToNativeWallet
import com.walletconnect.modalcore.utils.isLandscape

@Composable
internal fun ConnectYourWalletRoute(
    navController: NavController,
    uri: String,
    wallets: List<Wallet>
) {
    val uriHandler = LocalUriHandler.current

    ConnectYourWalletContent(
        wallets = wallets,
        onWalletItemClick = {
            uriHandler.goToNativeWallet(uri, it.nativeLink, it.universalLink, it.playStoreLink)
        },
        onViewAllClick = { navController.navigate(Route.AllWallets.path) },
        onScanIconClick = { navController.navigate(Route.ScanQRCode.path) }
    )
}

@Composable
private fun ConnectYourWalletContent(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit,
    onViewAllClick: () -> Unit,
    onScanIconClick: () -> Unit,
) {
    Column {
        ModalTopBar(title = "Connect your wallet", endIcon = {
            Image(imageVector = ImageVector.vectorResource(id = R.drawable.ic_scan),
                colorFilter = ColorFilter.tint(ModalTheme.colors.main),
                contentDescription = "Scan Icon",
                modifier = Modifier.clickable { onScanIconClick() })
        })
        WalletsGrid(
            wallets = wallets,
            onWalletItemClick = onWalletItemClick,
            onViewAllClick = onViewAllClick,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun WalletsGrid(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit,
    onViewAllClick: () -> Unit
) {
    val isLandscape = isLandscape
    if (wallets.isNotEmpty()) {
        WalletsLazyGridView(
            modifier = Modifier.fillMaxWidth(),
        ) { walletsInColumn ->
            if (wallets.size <= walletsInColumn) {
                walletsGridItems(wallets, onWalletItemClick)
            } else {
                walletsGridItemsWithViewAll(
                    if (isLandscape) walletsInColumn else walletsInColumn*2,
                    wallets,
                    onWalletItemClick,
                    onViewAllClick
                )
            }
        }
    }
}
private fun LazyGridScope.walletsGridItemsWithViewAll(
    maxGridElementsSize: Int,
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit,
    onViewAllClick: () -> Unit
) {
    val walletsSize = maxGridElementsSize - 1
    itemsIndexed(wallets.take(walletsSize)) { _, wallet ->
        WalletListItem(
            wallet = wallet,
            onWalletItemClick = onWalletItemClick
        )
    }
    item {
        ViewAllItem(wallets.subList(walletsSize, wallets.size), onViewAllClick)
    }
}

@Composable
private fun ViewAllItem(
    wallets: List<Wallet>,
    onViewAllClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable { onViewAllClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .size(80.dp)
                .padding(10.dp)
                .background(ModalTheme.colors.secondaryBackgroundColor, shape = RoundedCornerShape(10.dp))
                .border(1.dp, ModalTheme.colors.dividerColor, shape = RoundedCornerShape(10.dp)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            wallets.chunked(2).forEach {
                Row {
                    it.forEach { item ->
                        WalletImage(
                            url = item.imageUrl, Modifier
                                .size(30.dp)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                }
            }

        }
        Text(text = "View All", style = TextStyle(color = ModalTheme.colors.onBackgroundColor, fontSize = 12.sp))
    }
}

@Preview
@Composable
private fun ConnectYourWalletPreview() {
    ModalPreview {
        ConnectYourWalletContent(listOf(), {}, {}, {})
    }
}
