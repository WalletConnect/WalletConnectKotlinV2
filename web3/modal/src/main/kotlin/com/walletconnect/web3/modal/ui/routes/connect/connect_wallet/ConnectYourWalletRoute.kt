package com.walletconnect.web3.modal.ui.routes.connect.connect_wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalTopBar
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.domain.model.Wallet
import com.walletconnect.web3.modal.ui.components.internal.commons.WalletImage
import com.walletconnect.web3.modal.ui.components.internal.commons.WalletListItem
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import com.walletconnect.web3.modal.utils.goToNativeWallet

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
            uriHandler.goToNativeWallet(uri, it)
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
        Web3ModalTopBar(title = "Connect your wallet", endIcon = {
            Image(imageVector = ImageVector.vectorResource(id = R.drawable.ic_scan),
                colorFilter = ColorFilter.tint(Web3ModalTheme.colors.main),
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
    if (wallets.isNotEmpty()) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth(),
            columns = GridCells.Fixed(4)
        ) {
            items(7) {
                WalletListItem(
                    wallet = wallets[it],
                    onWalletItemClick = onWalletItemClick
                )
            }
            item {
                ViewAllItem(wallets.takeLast(4), onViewAllClick)
            }
        }
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
                .background(Web3ModalTheme.colors.secondaryBackgroundColor, shape = RoundedCornerShape(10.dp))
                .border(1.dp, Web3ModalTheme.colors.dividerColor, shape = RoundedCornerShape(10.dp)),
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
        Text(text = "View All", style = TextStyle(color = Web3ModalTheme.colors.onBackgroundColor, fontSize = 12.sp))
    }
}

@Preview
@Composable
private fun ConnectYourWalletPreview() {
    Web3ModalPreview {
        ConnectYourWalletContent(listOf(), {}, {}, {})
    }
}
