package com.walletconnect.web3.modal.ui.components.internal.commons.entry

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.TransparentSurface
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

internal data class EntryColors(
    val background: Color,
    val textColor: Color,
    val secondaryColor: Color
)

@Composable
internal fun BaseEntry(
    isEnabled: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable (EntryColors) -> Unit
) {
    val background: Color
    val textColor: Color
    val secondaryColor: Color
    if (isEnabled) {
        background = Web3ModalTheme.colors.overlay05
        textColor = Web3ModalTheme.colors.foreground.color100
        secondaryColor = Web3ModalTheme.colors.foreground.color200
    } else {
        background = Web3ModalTheme.colors.overlay15
        textColor = Web3ModalTheme.colors.overlay15
        secondaryColor = Web3ModalTheme.colors.overlay15
    }
    val entryColors = EntryColors(background, textColor, secondaryColor)

    TransparentSurface(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(contentPadding)
    ) {
        content(entryColors)
    }
}
