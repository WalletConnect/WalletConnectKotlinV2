package com.walletconnect.wcmodal.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

@Composable
fun WalletConnectModalTheme(
    accentColor: Color = Color(0xFF3496ff),
    onAccentColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    val customComposition = CustomizableComposition(
        accentColor = accentColor,
        onAccentColor = onAccentColor
    )
    CompositionLocalProvider(
        LocalCustomComposition provides customComposition
    ) {
        content()
    }
}

@Composable
internal fun ProvideModalThemeComposition(content: @Composable () -> Unit) {
    val composition = LocalCustomComposition.current

    val colors = provideModalColors(composition)
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
