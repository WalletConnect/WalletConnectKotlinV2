package com.walletconnect.sample.dapp.web3modal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

data class Web3ModalColors(
    val mainColor: Color,
    val onMainColor: Color,
    val onBackgroundColor: Color,
    val background: Color,
    val textColor: Color,
    val secondaryTextColor: Color,
)

@Composable
fun provideDefaultColors(): Web3ModalColors =
    if (isSystemInDarkTheme()) {
        darkWeb3ModalColors
    } else {
        lightWeb3ModalColors
    }

val lightWeb3ModalColors = Web3ModalColors(
    mainColor = Color(0xFF3496ff),
    onMainColor = Color.White,
    onBackgroundColor = Color.Black,
    background = Color.White,
    textColor = Color.Black,
    secondaryTextColor = Color(0xFF798686)
)

val darkWeb3ModalColors = Web3ModalColors(
    mainColor = Color(0xFF3496ff),
    onMainColor = Color.White,
    background = Color.Black,
    onBackgroundColor = Color.White,
    textColor = Color.White,
    secondaryTextColor = Color(0xFF949E9E)

)

object Web3ModalTheme {
    val colors: Web3ModalColors
        @Composable
        get() = LocalColorsComposition.current
}

@Composable
fun ProvideWeb3ModalThemeComposition(colors: Web3ModalColors, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalColorsComposition provides colors,
        content = content
    )
}

private val LocalColorsComposition = compositionLocalOf<Web3ModalColors> {
    error("No colors provided")
}
