package com.walletconnect.web3.modal.ui.components.internal.walletconnect

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.ListSelectRow
import com.walletconnect.web3.modal.ui.components.internal.commons.AllWalletsIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.TextLabel
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview

fun LazyListScope.allWallets(text: String, isEnabled: Boolean = true, onClick: () -> Unit) {
    item { WalletConnectAll(text = text, isEnabled = isEnabled, onClick = onClick) }
}

@Composable
private fun WalletConnectAll(text: String, isEnabled: Boolean = true, onClick: () -> Unit) {
    ListSelectRow(
        startIcon = { AllWalletsIcon() },
        text = "All wallets",
        label = { TextLabel(text = text, isEnabled = isEnabled) },
        onClick = onClick,
        contentPadding = PaddingValues(vertical = 4.dp)
    )
}

@UiModePreview
@Composable
private fun WalletConnectListSelectPreview() {
    MultipleComponentsPreview(
        { WalletConnectAll(text = "240+") {} },
        { WalletConnectAll(text = "240+", isEnabled = false) {} },
    )
}