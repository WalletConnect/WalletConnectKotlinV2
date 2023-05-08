package com.walletconnect.web3.modal.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

@Composable
internal fun ProvideWeb3ModalThemeComposition(
    colors: Web3ModalColors,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColorsComposition provides colors,
        content = content
    )
}

internal object Web3ModalTheme {
    val colors: Web3ModalColors
        @Composable
        get() = LocalColorsComposition.current
}

private val LocalColorsComposition = compositionLocalOf<Web3ModalColors> {
    error("No colors provided")
}
