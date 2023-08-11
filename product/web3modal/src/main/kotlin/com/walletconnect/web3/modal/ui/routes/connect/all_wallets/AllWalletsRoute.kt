package com.walletconnect.web3.modal.ui.routes.connect.all_wallets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.modal.utils.goToNativeWallet
import com.walletconnect.web3.modal.ui.components.internal.commons.walletsGridItems
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview

@Composable
internal fun AllWalletsRoute(
    navController: NavController,
    uri: String,
    wallets: List<Wallet>
) {
    val uriHandler = LocalUriHandler.current

    AllWalletsContent(
        wallets = wallets,
        onWalletItemClick = { uriHandler.goToNativeWallet(uri, it.nativeLink) },
    )
}

@Composable
private fun AllWalletsContent(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit,
) {
    Column {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxHeight(.8f)
                .padding(horizontal = 4.dp),
            columns = GridCells.Fixed(4)
        ) {
            walletsGridItems(wallets, onWalletItemClick)
        }
    }

}


@Preview
@Composable
private fun AllWalletsPreview() {
    Web3ModalPreview {
        AllWalletsContent(listOf(), {})
    }
}
