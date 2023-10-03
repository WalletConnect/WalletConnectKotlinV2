package com.walletconnect.web3.modal.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

@Composable
internal fun ProvideWeb3ModalThemeComposition(
    content: @Composable () -> Unit,
) {
    val composition = LocalCustomComposition.current
    val colors = provideWeb3ModalColors(composition)
    val typography = provideDefaultTypography(colors)
    CompositionLocalProvider(
        LocalColorsComposition provides colors,
        LocalTypographyComposition provides typography,
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
