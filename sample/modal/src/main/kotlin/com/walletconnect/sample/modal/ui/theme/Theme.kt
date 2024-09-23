package com.walletconnect.sample.modal.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

internal val darkColorScheme = darkColors(
    primary = Color(0xFF47A1FF),
    secondary = Color.White,
    background = Color(0xFF141414),
    onPrimary = Color.White
)

internal val lightColorScheme = lightColors(
    primary = Color(0xFF47A1FF),
    secondary = Color(0xFF141414),
    background = Color.White,
    onPrimary = Color(0xFF141414)
)

@Deprecated("com.walletconnect.web3.modal.ui.theme.WalletConnectTheme has been deprecated. Please use com.reown.appkit.modal.ui.theme.WalletConnectTheme instead from - https://github.com/reown-com/reown-kotlin")
@Composable
fun WalletConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorScheme else lightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colors = colorScheme,
        content = content
    )
}