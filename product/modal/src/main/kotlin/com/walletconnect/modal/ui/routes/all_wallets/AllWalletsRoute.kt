package com.walletconnect.modal.ui.routes.all_wallets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.modal.ui.components.ModalSearchTopBar
import com.walletconnect.modal.ui.components.WalletsLazyGridView
import com.walletconnect.modal.ui.components.walletsGridItems
import com.walletconnect.modal.ui.preview.ModalPreview
import com.walletconnect.modalcore.utils.goToNativeWallet
import com.walletconnect.modalcore.utils.isLandscape
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
        onBackClick = navController::popBackStack
    )
}

@Composable
private fun AllWalletsContent(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit,
    onBackClick: () -> Unit,
) {
    val gridFraction = if (isLandscape) 1f else .8f
    var searchInputValue by rememberSaveable() { mutableStateOf("") }

    Column {
        ModalSearchTopBar(
            searchValue = searchInputValue,
            onSearchValueChange = {
                searchInputValue = it
            },
            onBackPressed = onBackClick,
        )
        WalletsLazyGridView(
            modifier = Modifier
                .fillMaxHeight(gridFraction)
                .padding(horizontal = 4.dp),
        ) {
            walletsGridItems(wallets.filteredWallets(searchInputValue), onWalletItemClick)
        }
    }

}

private fun List<Wallet>.filteredWallets(value: String): List<Wallet> = this.filter { it.name.startsWith(prefix = value, ignoreCase = true) }


@Preview
@Composable
private fun AllWalletsPreview() {
    ModalPreview {
        AllWalletsContent(listOf(), {}, {})
    }
}
