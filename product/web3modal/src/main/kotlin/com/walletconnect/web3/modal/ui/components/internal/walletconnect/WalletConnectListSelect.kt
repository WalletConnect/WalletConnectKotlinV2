package com.walletconnect.web3.modal.ui.components.internal.walletconnect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.ListSelectRow
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

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

@Composable
private fun QrCodeLabel(isEnabled: Boolean = true) {
    val textColor: Color
    val background: Color
    if (isEnabled) {
        background = Web3ModalTheme.colors.main15
        textColor = Web3ModalTheme.colors.main100
    } else {
        background = Web3ModalTheme.colors.overlay10
        textColor = Web3ModalTheme.colors.foreground.color300
    }
    Box(
        modifier = Modifier
            .background(background, shape = RoundedCornerShape(4.dp))
            .padding(vertical = 4.dp, horizontal = 6.dp)
    ) {
        Text(text = "QR CODE", style = Web3ModalTheme.typo.micro700.copy(textColor))
    }
}

@UiModePreview
@Composable
private fun WalletConnectListSelectPreview() {
    MultipleComponentsPreview(
        { QrCodeLabel() },
        { QrCodeLabel(false) },
        { WalletConnectQRCode() {} },
        { WalletConnectQRCode(isEnabled = false) {} },
    )
}