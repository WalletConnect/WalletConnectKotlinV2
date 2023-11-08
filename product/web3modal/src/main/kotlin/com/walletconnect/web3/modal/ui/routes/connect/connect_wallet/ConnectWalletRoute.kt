package com.walletconnect.web3.modal.ui.routes.connect.connect_wallet

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.android.internal.common.modal.data.model.Wallet
import com.walletconnect.web3.modal.ui.components.internal.ErrorModalState
import com.walletconnect.web3.modal.ui.components.internal.commons.ListSelectRow
import com.walletconnect.web3.modal.ui.components.internal.commons.RecentLabel
import com.walletconnect.web3.modal.ui.components.internal.commons.WalletImage
import com.walletconnect.web3.modal.ui.components.internal.walletconnect.allWallets
import com.walletconnect.web3.modal.ui.model.UiStateBuilder
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.previews.ConnectYourWalletPreviewProvider
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.routes.connect.ConnectState
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun ConnectWalletRoute(
    connectState: ConnectState,
) {
    UiStateBuilder(
        connectState.uiState,
        onError = { ErrorModalState { connectState.fetchInitialWallets() } }
    ) {
        ConnectWalletContent(
            wallets = it,
            walletsTotalCount = connectState.getWalletsTotalCount(),
            onWalletItemClick = { wallet -> connectState.navigateToRedirectRoute(wallet) },
            onViewAllClick = { connectState.navigateToAllWallets() },
        )
    }
}

@Composable
private fun ConnectWalletContent(
    wallets: List<Wallet>,
    walletsTotalCount: Int,
    onWalletItemClick: (Wallet) -> Unit,
    onViewAllClick: () -> Unit,
) {
    WalletsList(
        wallets = wallets,
        walletsTotalCount = walletsTotalCount,
        onWalletItemClick = onWalletItemClick,
        onViewAllClick = onViewAllClick,
    )
}

@Composable
private fun WalletsList(
    wallets: List<Wallet>,
    walletsTotalCount: Int,
    onWalletItemClick: (Wallet) -> Unit,
    onViewAllClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        itemsIndexed(items = wallets.take(4)) { _, item ->
            WalletListSelect(item, onWalletItemClick)
        }
        allWallets(text = walletSizeLabel(walletsTotalCount), onClick = onViewAllClick)
    }
}

private fun walletSizeLabel(total: Int): String = with(total % 10) {
    if (this != 0) {
        "${total - this}+"
    } else {
        total.toString()
    }
}

@Composable
private fun WalletListSelect(item: Wallet, onWalletItemClick: (Wallet) -> Unit) {
    val label: (@Composable (Boolean) -> Unit)? = when {
        item.isRecent -> {
            { RecentLabel(it) }
        }
        else -> null
    }

    ListSelectRow(
        startIcon = {
            WalletImage(
                url = item.imageUrl,
                modifier = Modifier
                    .size(40.dp)
                    .border(width = 1.dp, color = Web3ModalTheme.colors.grayGlass10, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
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
        ConnectWalletContent(wallets, 200, {}, {})
    }
}
