package com.walletconnect.web3.modal.ui.routes.connect.connect_wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.modal.utils.goToNativeWallet
import com.walletconnect.web3.modal.ui.components.internal.commons.WalletImage
import com.walletconnect.web3.modal.ui.components.internal.commons.WalletGridItem
import com.walletconnect.web3.modal.ui.components.internal.commons.walletsGridItems
import com.walletconnect.web3.modal.ui.components.internal.walletconnect.WalletConnectLogo
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

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
            uriHandler.goToNativeWallet(uri, it.nativeLink)
        },
        onViewAllClick = { navController.navigate(Route.ALL_WALLETS.path) },
        onScanIconClick = { navController.navigate(Route.QR_CODE.path) }
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
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        if (wallets.isNotEmpty()) {

        } else {
            item {  }
        }
    }
    if (wallets.isNotEmpty()) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxWidth(),
            columns = GridCells.Fixed(4)
        ) {
            if (wallets.size <= 8) {
                walletsGridItems(wallets, onWalletItemClick)
            } else {
                walletsGridItemsWithViewAll(wallets, onWalletItemClick, onViewAllClick)
            }
        }
    }
}

private fun LazyGridScope.walletsGridItemsWithViewAll(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit,
    onViewAllClick: () -> Unit
) {
    itemsIndexed(wallets.take(7)) { _, wallet ->
        WalletGridItem(
            wallet = wallet,
            onWalletItemClick = onWalletItemClick
        )
    }
    item {
        ViewAllItem(wallets.subList(7, wallets.size), onViewAllClick)
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
                .background(Web3ModalTheme.colors.background.color200, shape = RoundedCornerShape(10.dp))
                .border(1.dp, Web3ModalTheme.colors.foreground.color125, shape = RoundedCornerShape(10.dp)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            wallets.chunked(2).forEach {
                Row() {
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
        Text(text = "View All", style = TextStyle(color = Web3ModalTheme.colors.foreground.color100, fontSize = 12.sp))
    }
}

@Preview
@Composable
private fun ConnectYourWalletPreview() {
    Web3ModalPreview {
        ConnectYourWalletContent(listOf(), {}, {}, {})
    }
}
