package com.walletconnect.web3.modal.ui.components.internal.commons.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.ForwardIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.HorizontalSpacer
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.testWallets
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun StoreEntry(
    text: String,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val background: Color
    val textColor: Color
    if (isEnabled) {
        textColor = Web3ModalTheme.colors.foreground.color200
        background = Web3ModalTheme.colors.grayGlass02
    } else {
        textColor = Web3ModalTheme.colors.foreground.color300
        background = Web3ModalTheme.colors.grayGlass10
    }
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { onClick() }
                .background(background).padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text, style = Web3ModalTheme.typo.paragraph500.copy(color = textColor), modifier = Modifier.weight(1f))
            HorizontalSpacer(width = 10.dp)
            GetButton()
        }
    }
}

@Composable
private fun GetButton() {
    Row(
        modifier = Modifier
            .border(width = 1.dp, color = Web3ModalTheme.colors.grayGlass10, RoundedCornerShape(100))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = "Get", style = Web3ModalTheme.typo.small500.copy(color = Web3ModalTheme.colors.accent100))
        ForwardIcon(tint = Web3ModalTheme.colors.accent100)
    }
}

@UiModePreview
@Composable
private fun PreviewStoreEntry() {
    val wallet = testWallets.first()
    MultipleComponentsPreview(
        { StoreEntry(text = "Don't have ${wallet.name}?") {} },
        { StoreEntry(text = "Don't have ${wallet.name}?", isEnabled = false) {} },
    )
}
