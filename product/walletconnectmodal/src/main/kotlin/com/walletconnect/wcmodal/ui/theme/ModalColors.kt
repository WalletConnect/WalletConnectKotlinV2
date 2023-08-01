package com.walletconnect.wcmodal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal data class ModalColors(
    val main: Color,
    val onMainColor: Color,
    val background: Color,
    val onBackgroundColor: Color,
    val secondaryBackgroundColor: Color,
    val textColor: Color,
    val secondaryTextColor: Color,
    val dividerColor: Color,
    val border: Color,
    val errorColor: Color = Color(0xFFF05142)
)

@Composable
internal fun provideModalColors(
    composition: CustomizableComposition
): ModalColors = if (isSystemInDarkTheme()) {
    defaultDarkWeb3ModalColors(composition.accentColor, composition.onAccentColor)
} else {
    defaultLightWeb3ModalColors(composition.accentColor, composition.onAccentColor)
}

private fun defaultLightWeb3ModalColors(
    mainColor: Color,
    onMainColor: Color
) = ModalColors(
    main = mainColor,
    onMainColor = onMainColor,
    onBackgroundColor = Color.Black,
    background = Color.White,
    textColor = Color.Black,
    secondaryTextColor = Color(0xFF798686),
    secondaryBackgroundColor = Color(0xFFF1F3F3),
    dividerColor = Color(0xFFE4E7E7),
    border = Color(0x32062B2B)
)

private fun defaultDarkWeb3ModalColors(
    mainColor: Color,
    onMainColor: Color
) = ModalColors(
    main = mainColor,
    onMainColor = onMainColor,
    background = Color.Black,
    onBackgroundColor = Color.White,
    textColor = Color.White,
    secondaryTextColor = Color(0xFF949E9E),
    secondaryBackgroundColor = Color(0xFF272A2A),
    dividerColor = Color(0xFF3B4040),
    border = Color(0x32062B2B)
)
