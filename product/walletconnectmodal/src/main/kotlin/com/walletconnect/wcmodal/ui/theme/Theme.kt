package com.walletconnect.wcmodal.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

@Composable
internal fun ProvideModalThemeComposition(
    content: @Composable () -> Unit,
) {
    val colors = provideModalColors()
    CompositionLocalProvider(
        LocalColorsComposition provides colors,
        content = content
    )
}

internal object ModalTheme {
    val colors: ModalColors
        @Composable
        get() = LocalColorsComposition.current
}

private val LocalColorsComposition = compositionLocalOf<ModalColors> {
    error("No colors provided")
}
