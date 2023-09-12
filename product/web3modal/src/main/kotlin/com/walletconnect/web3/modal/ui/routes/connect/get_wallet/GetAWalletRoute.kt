package com.walletconnect.web3.modal.ui.routes.connect.get_wallet

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.walletconnect.modal.utils.openPlayStore
import com.walletconnect.web3.modal.domain.model.Wallet
import com.walletconnect.web3.modal.ui.components.internal.commons.ExternalIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.ListSelectRow
import com.walletconnect.web3.modal.ui.components.internal.commons.MultipleWalletIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.WalletImage
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.previews.testWallets

@Composable
internal fun GetAWalletRoute(wallets: List<Wallet>) {
    GetAWalletContent(
        wallets = wallets,
    )
}

@Composable
private fun GetAWalletContent(
    wallets: List<Wallet>,
) {
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        itemsIndexed(wallets.take(5)) { _, wallet ->
            ListSelectRow(
                startIcon = {
                    WalletImage(
                        url = wallet.imageUrl,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                },
                text = wallet.name,
                contentPadding = PaddingValues(vertical = 4.dp),
                onClick = { uriHandler.openPlayStore(wallet.playStore) }
            )
        }
        item {
            ListSelectRow(
                startIcon = { MultipleWalletIcon(wallets = wallets.take(4)) },
                text = "Explore All",
                contentPadding = PaddingValues(vertical = 4.dp),
                label = { ExternalIcon() },
                onClick = { uriHandler.openUri("https://explorer.walletconnect.com/?type=wallet") }
            )
        }
    }
}

@UiModePreview
@Composable
private fun PreviewGetAWallet() {
    Web3ModalPreview(title = "Get a Wallet") {
        GetAWalletContent(wallets = testWallets)
    }
}

