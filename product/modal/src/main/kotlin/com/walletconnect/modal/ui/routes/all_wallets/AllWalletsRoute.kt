package com.walletconnect.modal.ui.routes.all_wallets

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.modal.R
import com.walletconnect.modal.ui.components.ModalTopBar
import com.walletconnect.modal.ui.components.WalletsLazyGridView
import com.walletconnect.modal.ui.components.walletsGridItems
import com.walletconnect.modal.ui.navigation.Route
import com.walletconnect.modal.ui.preview.ModalPreview
import com.walletconnect.modal.ui.theme.ModalTheme
import com.walletconnect.modalcore.utils.goToNativeWallet
import com.walletconnect.modalcore.utils.isLandscape

@Composable
internal fun AllWalletsRoute(
    navController: NavController,
    uri: String,
    wallets: List<Wallet>
) {
    val uriHandler = LocalUriHandler.current

    AllWalletsContent(
        wallets = wallets,
        onWalletItemClick = {
            uriHandler.goToNativeWallet(uri, it.nativeLink, it.universalLink, it.playStoreLink)
        },
        onScanIconClick = { navController.navigate(Route.ScanQRCode.path) },
        onBackClick = navController::popBackStack
    )
}

@Composable
private fun AllWalletsContent(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit,
    onScanIconClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val gridFraction = if (isLandscape) 1f else .8f
    Column {
        ModalTopBar(
            title = "Connect your wallet",
            onBackPressed = onBackClick,
            endIcon = {
                Image(imageVector = ImageVector.vectorResource(id = R.drawable.ic_scan),
                    colorFilter = ColorFilter.tint(ModalTheme.colors.main),
                    contentDescription = "Scan Icon",
                    modifier = Modifier.clickable { onScanIconClick() })
            }
        )
        WalletsLazyGridView(
            modifier = Modifier
                .fillMaxHeight(gridFraction)
                .padding(horizontal = 4.dp),
        ) {
            walletsGridItems(wallets, onWalletItemClick)
        }
    }

}


@Preview
@Composable
private fun AllWalletsPreview() {
    ModalPreview {
        AllWalletsContent(listOf(), {}, {}, {})
    }
}
