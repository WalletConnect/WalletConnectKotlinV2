package com.walletconnect.web3.modal.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

@Composable
internal fun ProvideWeb3ModalThemeComposition(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColorsComposition provides provideWeb3ModalColors(),
        LocalTypographyComposition provides defaultTypography,
        content = content
    )
}

internal object Web3ModalTheme {
    val colors: Web3ModalColors
        @Composable
        get() = LocalColorsComposition.current

    val typo: Web3ModalTypography
        @Composable
        get() = LocalTypographyComposition.current
}

private val LocalTypographyComposition = compositionLocalOf<Web3ModalTypography> {
    error("No typography provided")
}

private val LocalColorsComposition = compositionLocalOf<Web3ModalColors> {
    error("No colors provided")
}
