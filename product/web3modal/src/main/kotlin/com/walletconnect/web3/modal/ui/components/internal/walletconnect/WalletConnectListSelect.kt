package com.walletconnect.web3.modal.ui.components.internal.walletconnect

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.ListSelectRow
import com.walletconnect.web3.modal.ui.components.internal.commons.QrCodeLabel
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview

fun LazyListScope.walletConnectQRCode(isEnabled: Boolean = true, onClick: () -> Unit) {
    item { WalletConnectQRCode(isEnabled = isEnabled, onClick = onClick) }
}

@Composable
private fun WalletConnectQRCode(isEnabled: Boolean = true, onClick: () -> Unit) {
    ListSelectRow(
        startIcon = { WalletConnectLogo(isEnabled) },
        text = "WalletConnect",
        label = { QrCodeLabel(isEnabled) },
        onClick = onClick,
        contentPadding = PaddingValues(vertical = 4.dp)
    )
}

@UiModePreview
@Composable
private fun WalletConnectListSelectPreview() {
    MultipleComponentsPreview(
        { WalletConnectQRCode() {} },
        { WalletConnectQRCode(isEnabled = false) {} },
    )
}