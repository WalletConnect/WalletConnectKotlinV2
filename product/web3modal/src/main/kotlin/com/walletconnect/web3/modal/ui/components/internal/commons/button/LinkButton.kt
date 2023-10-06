package com.walletconnect.web3.modal.ui.components.internal.commons.button

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.HorizontalSpacer

@Composable
internal fun LinkButton(
    text: String,
    startIcon: @Composable (Color) -> Unit,
    size: ButtonSize,
    isEnabled: Boolean = true,
    onClick: () -> Unit,
) {
    StyledButton(
        style = ButtonStyle.LINK,
        size = size,
        isEnabled = isEnabled,
        onClick = onClick
    ) { buttonData ->
        startIcon(buttonData.tint)
        HorizontalSpacer(width = 4.dp)
        Text(text = text, style = buttonData.textStyle)
    }
}