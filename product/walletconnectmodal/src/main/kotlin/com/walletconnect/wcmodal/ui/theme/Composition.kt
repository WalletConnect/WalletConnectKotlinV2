package com.walletconnect.wcmodal.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

internal data class CustomizableComposition(
    val accentColor: Color = Color(0xFF3496ff),
    val onAccentColor: Color = Color.White
)

internal val LocalCustomComposition = compositionLocalOf { CustomizableComposition() }