package com.walletconnect.web3.modal.ui.routes.connect.connect_wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.web3.modal.ui.components.internal.commons.InstalledLabel
import com.walletconnect.web3.modal.ui.components.internal.commons.ListSelectRow
import com.walletconnect.web3.modal.ui.components.internal.commons.MultipleWalletIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.RecentLabel
import com.walletconnect.web3.modal.ui.components.internal.commons.WalletImage
import com.walletconnect.web3.modal.ui.components.internal.walletconnect.walletConnectAllWallets
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.previews.ConnectYourWalletPreviewProvider
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview

@Composable
internal fun ConnectWalletRoute(
    navController: NavController,
    wallets: List<Wallet>
) {
    ConnectWalletContent(
        wallets = wallets,
        onWalletItemClick = { navController.navigate(Route.REDIRECT.path + "/${it.id}&${it.name}") },
        onViewAllClick = { navController.navigate(Route.ALL_WALLETS.path) },
    )
}

@Composable
private fun ConnectWalletContent(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit,
    onViewAllClick: () -> Unit,
) {
    Column {
        WalletsList(
            wallets = wallets,
            onWalletItemClick = onWalletItemClick,
            onViewAllClick = onViewAllClick,
        )
    }
}

@Composable
private fun WalletsList(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit,
    onViewAllClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        walletConnectAllWallets(onClick = onViewAllClick)
        itemsIndexed(items = wallets.take(3)) { _, item ->
            WalletListSelect(item, onWalletItemClick)
        }
    }
}


@Composable
private fun WalletListSelect(item: Wallet, onWalletItemClick: (Wallet) -> Unit) {
    val label: (@Composable (Boolean) -> Unit)? = when {
        item.isRecent -> {
            { RecentLabel(it) }
        }
        item.isWalletInstalled -> {
            { InstalledLabel(it) }
        }
        else -> null
    }

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
        contentPadding = PaddingValues(vertical = 4.dp),
        label = label
    )
}

@UiModePreview
@Composable
private fun ConnectYourWalletPreview(
    @PreviewParameter(ConnectYourWalletPreviewProvider::class) wallets: List<Wallet>
) {
    Web3ModalPreview(title = "Connect Wallet") {
        ConnectWalletContent(wallets, {}, {})
    }
}
