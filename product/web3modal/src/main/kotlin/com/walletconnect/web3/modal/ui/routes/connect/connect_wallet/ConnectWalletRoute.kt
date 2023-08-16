package com.walletconnect.web3.modal.ui.routes.connect.connect_wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.modal.utils.goToNativeWallet
import com.walletconnect.web3.modal.ui.components.internal.commons.ListSelectRow
import com.walletconnect.web3.modal.ui.components.internal.commons.WalletImage
import com.walletconnect.web3.modal.ui.components.internal.walletconnect.walletConnectQRCode
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.previews.ConnectYourWalletPreviewProvider
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun ConnectWalletRoute(
    navController: NavController,
    uri: String,
    wallets: List<Wallet>
) {
    val uriHandler = LocalUriHandler.current

    ConnectWalletContent(
        wallets = wallets,
        onWalletItemClick = {
            uriHandler.goToNativeWallet(uri, it.nativeLink)
        },
        onViewAllClick = { navController.navigate(Route.ALL_WALLETS.path) },
        onScanIconClick = { navController.navigate(Route.QR_CODE.path) }
    )
}

@Composable
private fun ConnectWalletContent(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit,
    onViewAllClick: () -> Unit,
    onScanIconClick: () -> Unit,
) {
    Column {
        WalletsList(
            wallets = wallets,
            onWalletItemClick = onWalletItemClick,
            onViewAllClick = onViewAllClick,
            onScanIconClick = onScanIconClick
        )
    }
}

@Composable
private fun WalletsList(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit,
    onViewAllClick: () -> Unit,
    onScanIconClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (wallets.isNotEmpty()) {
            if (wallets.size <= 3) {
                itemsIndexed(items = wallets) { _, item -> WalletListSelect(item, onWalletItemClick) }
                walletConnectQRCode(onClick = onScanIconClick)
            } else {
                walletsItemsWithViewAll(wallets, onWalletItemClick, onViewAllClick, onScanIconClick)
            }
        } else {
            walletConnectQRCode(onClick = onScanIconClick)
        }
    }
}

private fun LazyListScope.walletsItemsWithViewAll(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit,
    onViewAllClick: () -> Unit,
    onScanIconClick: () -> Unit
) {
    itemsIndexed(wallets.take(3)) { _, wallet ->
        WalletListSelect(wallet, onWalletItemClick)
    }
    walletConnectQRCode(onClick = onScanIconClick)
    when(wallets.size) {
        4 -> item { WalletListSelect(item = wallets[3], onWalletItemClick = { onWalletItemClick(wallets[3])}) }
        else -> item { AllWalletListSelect(wallets.subList(3, wallets.size), onViewAllClick) }
    }
}

@Composable
private fun WalletListSelect(item: Wallet, onWalletItemClick: (Wallet) -> Unit) {
    ListSelectRow(
        startIcon = {
            WalletImage(
                url = item.imageUrl,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        },
        text = item.name,
        onClick = { onWalletItemClick(item) },
        contentPadding = PaddingValues(vertical = 4.dp)
    )
}

@Composable
private fun AllWalletListSelect(
    wallets: List<Wallet>,
    onViewAllClick: () -> Unit
) {
    ListSelectRow(
        startIcon = { AllWalletsIcons(wallets) },
        text = "All Wallets",
        onClick = onViewAllClick,
        contentPadding = PaddingValues(vertical = 4.dp)
    )
}

@Composable
private fun AllWalletsIcons(wallets: List<Wallet>) {
    Column(
        modifier = Modifier
            .size(40.dp)
            .background(Web3ModalTheme.colors.background.color200, shape = RoundedCornerShape(10.dp))
            .padding(1.dp)
            .border(1.dp, Web3ModalTheme.colors.overlay10, shape = RoundedCornerShape(10.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        wallets.chunked(2).forEach {
            Row {
                it.forEach { item ->
                    WalletImage(
                        url = item.imageUrl, Modifier
                            .size(20.dp)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
            }
        }

    }
}

@UiModePreview
@Composable
private fun ConnectYourWalletPreview(
    @PreviewParameter(ConnectYourWalletPreviewProvider::class) wallets: List<Wallet>
) {
    Web3ModalPreview(title = "Connect Wallet") {
        ConnectWalletContent(wallets, {}, {}, {})
    }
}
