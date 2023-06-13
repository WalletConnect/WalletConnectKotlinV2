package com.walletconnect.web3.modal.ui.routes.connect.connect_wallet

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalTopBar
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.domain.model.Wallet
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalTopBar
import com.walletconnect.web3.modal.ui.components.internal.commons.AutoScrollingWalletList
import com.walletconnect.web3.modal.ui.components.internal.commons.MainButton
import com.walletconnect.web3.modal.ui.components.internal.commons.RoundedOutLineButton
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import com.walletconnect.web3.modal.utils.toDeeplinkUri
import com.walletconnect.web3.modal.utils.toNativeDeeplinkUri

@Composable
internal fun ConnectYourWalletRoute(
    navController: NavController,
    uri: String,
    wallets: List<Wallet>
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    ConnectYourWalletContent(
        wallets = wallets,
        onWalletItemClick = {
            goToNativeWallet(context, uri, it) { uri -> uriHandler.openUri(uri) }
        },
        onViewAllClick = { },
        onScanIconClick = { navController.navigate(Route.ScanQRCode.path) },
        onGetAWalletClick = { navController.navigate(Route.GetAWallet.path) })
}

private fun goToNativeWallet(context: Context, uri: String, wallet: Wallet, openUri: (String) -> Unit) {
    try {
        when {
            !wallet.nativeLink.isNullOrBlank() -> {
                val a = wallet.nativeLink + uri.toNativeDeeplinkUri()
                println(a)
                openUri(wallet.nativeLink + uri.toNativeDeeplinkUri())
            }

            !wallet.universalLink.isNullOrBlank() -> openUri(wallet.universalLink)
            else -> openUri(wallet.playStoreLink)
        }
    } catch (e: Exception) {
        openUri(wallet.playStoreLink)
    }
}

@Composable
private fun ConnectYourWalletContent(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit,
    onViewAllClick: () -> Unit,
    onScanIconClick: () -> Unit,
    onGetAWalletClick: () -> Unit,
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
internal fun WalletListItem(
    wallet: Wallet,
    onWalletItemClick: (Wallet) -> Unit
) {
    Column(
        modifier = Modifier.clickable { onWalletItemClick(wallet) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WalletImage(
            url = wallet.imageUrl,
            modifier = Modifier
                .size(80.dp)
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Text(text = wallet.name, style = TextStyle(color = Web3ModalTheme.colors.onBackgroundColor, fontSize = 12.sp))
        VerticalSpacer(height = 16.dp)
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

@Composable
private fun WalletImage(url: String, modifier: Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = modifier
    )
}

@Preview
@Composable
private fun ConnectYourWalletPreview() {
    Web3ModalPreview {
        ConnectYourWalletContent(listOf(), {}, {}, {}, {})
    }
}
