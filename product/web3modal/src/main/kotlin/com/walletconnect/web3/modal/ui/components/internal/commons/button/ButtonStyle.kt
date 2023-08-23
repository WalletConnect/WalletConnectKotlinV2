package com.walletconnect.web3.modal.ui.components.internal.commons.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

data class ButtonData(
    val size: ButtonSize,
    val style: ButtonStyle,
    val textStyle: TextStyle,
    val tint: Color,
    val background: Color
)

enum class ButtonStyle { MAIN, ACCENT, }

enum class ButtonSize { M, S }

@Composable
internal fun ButtonSize.getTextStyle() = when (this) {
    ButtonSize.M -> Web3ModalTheme.typo.paragraph600
    ButtonSize.S -> Web3ModalTheme.typo.small600
}

@Composable
internal fun ButtonSize.getContentPadding() = when (this) {
    ButtonSize.M -> PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ButtonSize.S -> PaddingValues(horizontal = 12.dp, vertical = 6.dp)
}

@Composable
internal fun ButtonStyle.getTextColor(isEnabled: Boolean) = when(this) {
    ButtonStyle.MAIN -> if (isEnabled) Web3ModalTheme.colors.inverse100 else Web3ModalTheme.colors.foreground.color300
    ButtonStyle.ACCENT -> if (isEnabled) Web3ModalTheme.colors.main100 else Web3ModalTheme.colors.overlay20
}

@Composable
internal fun ButtonStyle.getBackgroundColor(isEnabled: Boolean) = when (this) {
    ButtonStyle.MAIN -> if (isEnabled) Web3ModalTheme.colors.main100 else Web3ModalTheme.colors.overlay20
    ButtonStyle.ACCENT -> if (isEnabled) Color.Transparent else Web3ModalTheme.colors.overlay10
}

@Composable
internal fun ButtonStyle.getBorder(isEnabled: Boolean) = when (this) {
    ButtonStyle.MAIN -> if (isEnabled) Color.Transparent else Web3ModalTheme.colors.overlay20
    ButtonStyle.ACCENT -> if (isEnabled) Web3ModalTheme.colors.overlay10 else Web3ModalTheme.colors.overlay05
}
