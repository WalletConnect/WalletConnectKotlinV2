package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.walletconnect.WalletConnectLogo
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun ListSelectRow(
    startIcon: @Composable (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    label: (@Composable (Boolean) -> Unit)? = null,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val background: Color
    val textColor: Color
    if (isEnabled) {
        background = Web3ModalTheme.colors.overlay05
        textColor = Web3ModalTheme.colors.foreground.color100
    } else {
        background = Web3ModalTheme.colors.overlay15
        textColor = Web3ModalTheme.colors.foreground.color300
    }
    Row(
        modifier = modifier
            .background(background, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        startIcon(isEnabled)
        HorizontalSpacer(width = 10.dp)
        Text(
            text = text,
            style = Web3ModalTheme.typo.paragraph500.copy(textColor),
            modifier = Modifier.weight(1f)
        )
        label?.let {
            HorizontalSpacer(width = 10.dp)
            it.invoke(isEnabled)
        }
    }
}

@UiModePreview
@Composable
private fun ListSelectRowPreview() {
    MultipleComponentsPreview(
        { ListSelectRow(startIcon = { WalletConnectLogo(it) }, text = "WalletConnect") {} },
        { ListSelectRow(startIcon = { WalletConnectLogo(it) }, text = "WalletConnect", isEnabled = false) {} }
    )
}