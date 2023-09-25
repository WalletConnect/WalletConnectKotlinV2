package com.walletconnect.web3.modal.ui.components.internal.commons.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

internal data class ButtonData(
    val size: ButtonSize,
    val style: ButtonStyle,
    val textStyle: TextStyle,
    val tint: Color,
    val background: Color
)

internal enum class ButtonStyle { MAIN, ACCENT, SHADE, LOADING, ACCOUNT }

internal enum class ButtonSize { M, S, ACCOUNT_M, ACCOUNT_S }

@Composable
internal fun ButtonSize.getTextStyle() = when (this) {
    ButtonSize.M, ButtonSize.ACCOUNT_M, ButtonSize.ACCOUNT_S -> Web3ModalTheme.typo.paragraph600
    ButtonSize.S -> Web3ModalTheme.typo.small600
}

@Composable
internal fun ButtonSize.getContentPadding() = when (this) {
    ButtonSize.M -> PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ButtonSize.S -> PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ButtonSize.ACCOUNT_M -> PaddingValues(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
    ButtonSize.ACCOUNT_S -> PaddingValues(start = 6.dp, end = 12.dp, top = 5.dp, bottom = 5.dp)
}

@Composable
internal fun ButtonStyle.getTextColor(isEnabled: Boolean) = when (this) {
    ButtonStyle.MAIN -> if (isEnabled) Web3ModalTheme.colors.inverse100 else Web3ModalTheme.colors.foreground.color300
    ButtonStyle.ACCENT, ButtonStyle.LOADING -> if (isEnabled) Web3ModalTheme.colors.main100 else Web3ModalTheme.colors.overlay20
    ButtonStyle.SHADE -> if (isEnabled) Web3ModalTheme.colors.foreground.color150 else Web3ModalTheme.colors.overlay15
    ButtonStyle.ACCOUNT -> if (isEnabled) Web3ModalTheme.colors.foreground.color100 else Web3ModalTheme.colors.overlay15
}

@Composable
internal fun ButtonStyle.getBackgroundColor(isEnabled: Boolean) = when (this) {
    ButtonStyle.MAIN -> if (isEnabled) Web3ModalTheme.colors.main100 else Web3ModalTheme.colors.overlay20
    ButtonStyle.ACCENT -> if (isEnabled) Color.Transparent else Web3ModalTheme.colors.overlay10
    ButtonStyle.SHADE -> if (isEnabled) Color.Transparent else Web3ModalTheme.colors.overlay05
    ButtonStyle.LOADING -> Web3ModalTheme.colors.overlay10
    ButtonStyle.ACCOUNT -> if (isEnabled) Web3ModalTheme.colors.overlay10 else Web3ModalTheme.colors.overlay15
}

@Composable
internal fun ButtonStyle.getBorder(isEnabled: Boolean) = when (this) {
    ButtonStyle.MAIN -> if (isEnabled) Color.Transparent else Web3ModalTheme.colors.overlay20
    ButtonStyle.ACCENT, ButtonStyle.SHADE, ButtonStyle.LOADING, ButtonStyle.ACCOUNT -> if (isEnabled) Web3ModalTheme.colors.overlay10 else Web3ModalTheme.colors.overlay05
}

internal data class ButtonPreview(
    val style: ButtonStyle,
    val size: ButtonSize,
    val isEnabled: Boolean
)
