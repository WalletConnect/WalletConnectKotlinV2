package com.walletconnect.web3.modal.ui.components.internal.walletconnect

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.ListSelectRow
import com.walletconnect.web3.modal.ui.components.internal.commons.AllLabel
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview

fun LazyListScope.walletConnectAllWallets(isEnabled: Boolean = true, onClick: () -> Unit) {
    item { WalletConnectAll(isEnabled = isEnabled, onClick = onClick) }
}

@Composable
private fun WalletConnectAll(isEnabled: Boolean = true, onClick: () -> Unit) {
    ListSelectRow(
        startIcon = { WalletConnectLogo(isEnabled) },
        text = "WalletConnect",
        label = { AllLabel(isEnabled) },
        onClick = onClick,
        contentPadding = PaddingValues(vertical = 4.dp)
    )
}

@UiModePreview
@Composable
private fun WalletConnectListSelectPreview() {
    MultipleComponentsPreview(
        { WalletConnectAll() {} },
        { WalletConnectAll(isEnabled = false) {} },
    )
}