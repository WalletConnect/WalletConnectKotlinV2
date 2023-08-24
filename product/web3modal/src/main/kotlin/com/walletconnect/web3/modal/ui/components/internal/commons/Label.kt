package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun QrCodeLabel(isEnabled: Boolean = true) {
    ListLabel(text = "QR CODE", isEnabled = isEnabled)
}

@Composable
internal fun GetWalletLabel(isEnabled: Boolean = true) {
    ListLabel(text = "GET WALLET", isEnabled = isEnabled)
}

@Composable
private fun ListLabel(
    text: String,
    isEnabled: Boolean
) {
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
        Text(text = text, style = Web3ModalTheme.typo.micro700.copy(textColor))
    }
}
