package com.walletconnect.web3.modal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class Web3ModalColors(
    val main: Color,
    val onMainColor: Color,
    val background: Color,
    val onBackgroundColor: Color,
    val secondaryBackgroundColor: Color,
    val textColor: Color,
    val secondaryTextColor: Color,
    val dividerColor: Color
)

@Composable
fun provideWeb3Colors(
    lightColors: Web3ModalColors = defaultLightWeb3ModalColors,
    darkColors: Web3ModalColors = defaultDarkWeb3ModalColors
): Web3ModalColors = if (isSystemInDarkTheme()) {
    darkColors
} else {
    lightColors
}

private val defaultLightWeb3ModalColors = Web3ModalColors(
    main = Color(0xFF3496ff),
    onMainColor = Color.White,
    onBackgroundColor = Color.Black,
    background = Color.White,
    textColor = Color.Black,
    secondaryTextColor = Color(0xFF798686),
    secondaryBackgroundColor = Color(0xFFF1F3F3),
    dividerColor = Color(0xFFE4E7E7)
)

private val defaultDarkWeb3ModalColors = Web3ModalColors(
    main = Color(0xFF3496ff),
    onMainColor = Color.White,
    background = Color.Black,
    onBackgroundColor = Color.White,
    textColor = Color.White,
    secondaryTextColor = Color(0xFF949E9E),
    secondaryBackgroundColor = Color(0xFF272A2A),
    dividerColor = Color(0xFF3B4040)
)
