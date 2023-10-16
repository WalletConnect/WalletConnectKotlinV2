package com.walletconnect.web3.modal.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.walletconnect.web3.modal.ui.theme.ColorPalette
import com.walletconnect.web3.modal.ui.theme.CustomComposition
import com.walletconnect.web3.modal.ui.theme.LocalCustomComposition
import com.walletconnect.web3.modal.ui.theme.defaultDarkWeb3ModalColors
import com.walletconnect.web3.modal.ui.theme.defaultLightWeb3ModalColors

@Composable
fun Web3ModalTheme(
    mode: Web3ModalTheme.Mode = Web3ModalTheme.Mode.AUTO,
    lightColors: Web3ModalTheme.Colors = Web3ModalTheme.provideLightWeb3ModalColors(),
    darkColors: Web3ModalTheme.Colors = Web3ModalTheme.provideDarkWeb3ModalColor(),
    content: @Composable () -> Unit
) {
    val customComposition = CustomComposition(
        mode = mode,
        lightColors = lightColors,
        darkColors = darkColors,
    )
    CompositionLocalProvider(
        LocalCustomComposition provides customComposition
    ) {
        content()
    }
}

object Web3ModalTheme {

    fun provideLightWeb3ModalColors(
        main100: Color = defaultLightWeb3ModalColors.main100,
        main90: Color = defaultLightWeb3ModalColors.main90,
        main20: Color = defaultLightWeb3ModalColors.main20,
        foreground: ColorPalette = defaultLightWeb3ModalColors.foreground,
        background: ColorPalette = defaultLightWeb3ModalColors.background,
        overlay: Color = defaultLightWeb3ModalColors.overlay,
        success: Color = defaultLightWeb3ModalColors.success,
        error: Color = defaultLightWeb3ModalColors.error
    ): Colors = CustomWeb3ModalColor(main100, main90, main20, foreground, background, overlay, success, error)

    fun provideDarkWeb3ModalColor(
        main100: Color = defaultDarkWeb3ModalColors.main100,
        main90: Color = defaultDarkWeb3ModalColors.main90,
        main20: Color = defaultDarkWeb3ModalColors.main20,
        foreground: ColorPalette = defaultDarkWeb3ModalColors.foreground,
        background: ColorPalette = defaultDarkWeb3ModalColors.background,
        overlay: Color = defaultDarkWeb3ModalColors.overlay,
        success: Color = defaultDarkWeb3ModalColors.success,
        error: Color = defaultDarkWeb3ModalColors.error
    ): Colors = CustomWeb3ModalColor(main100, main90, main20, foreground, background, overlay, success, error)

    fun provideForegroundLightColorPalette(
        color100: Color = defaultLightWeb3ModalColors.foreground.color100,
        color125: Color = defaultLightWeb3ModalColors.foreground.color125,
        color150: Color = defaultLightWeb3ModalColors.foreground.color150,
        color175: Color = defaultLightWeb3ModalColors.foreground.color175,
        color200: Color = defaultLightWeb3ModalColors.foreground.color200,
        color225: Color = defaultLightWeb3ModalColors.foreground.color225,
        color250: Color = defaultLightWeb3ModalColors.foreground.color250,
        color275: Color = defaultLightWeb3ModalColors.foreground.color275,
        color300: Color = defaultLightWeb3ModalColors.foreground.color300,
    ) = ColorPalette(color100, color125, color150, color175, color200, color225, color250, color275, color300)

    fun provideForegroundDarkColorPalette(
        color100: Color = defaultDarkWeb3ModalColors.foreground.color100,
        color125: Color = defaultDarkWeb3ModalColors.foreground.color125,
        color150: Color = defaultDarkWeb3ModalColors.foreground.color150,
        color175: Color = defaultDarkWeb3ModalColors.foreground.color175,
        color200: Color = defaultDarkWeb3ModalColors.foreground.color200,
        color225: Color = defaultDarkWeb3ModalColors.foreground.color225,
        color250: Color = defaultDarkWeb3ModalColors.foreground.color250,
        color275: Color = defaultDarkWeb3ModalColors.foreground.color275,
        color300: Color = defaultDarkWeb3ModalColors.foreground.color300,
    ) = ColorPalette(color100, color125, color150, color175, color200, color225, color250, color275, color300)

    fun provideBackgroundLightColorPalette(
        color100: Color = defaultLightWeb3ModalColors.background.color100,
        color125: Color = defaultLightWeb3ModalColors.background.color125,
        color150: Color = defaultLightWeb3ModalColors.background.color150,
        color175: Color = defaultLightWeb3ModalColors.background.color175,
        color200: Color = defaultLightWeb3ModalColors.background.color200,
        color225: Color = defaultLightWeb3ModalColors.background.color225,
        color250: Color = defaultLightWeb3ModalColors.background.color250,
        color275: Color = defaultLightWeb3ModalColors.background.color275,
        color300: Color = defaultLightWeb3ModalColors.background.color300,
    ) = ColorPalette(color100, color125, color150, color175, color200, color225, color250, color275, color300)

    fun provideBackgroundDarkColorPalette(
        color100: Color = defaultDarkWeb3ModalColors.background.color100,
        color125: Color = defaultDarkWeb3ModalColors.background.color125,
        color150: Color = defaultDarkWeb3ModalColors.background.color150,
        color175: Color = defaultDarkWeb3ModalColors.background.color175,
        color200: Color = defaultDarkWeb3ModalColors.background.color200,
        color225: Color = defaultDarkWeb3ModalColors.background.color225,
        color250: Color = defaultDarkWeb3ModalColors.background.color250,
        color275: Color = defaultDarkWeb3ModalColors.background.color275,
        color300: Color = defaultDarkWeb3ModalColors.background.color300,
    ) = ColorPalette(color100, color125, color150, color175, color200, color225, color250, color275, color300)


    enum class Mode {
        LIGHT, DARK, AUTO
    }

    interface Colors {
        val main100: Color
        val main90: Color
        val main20: Color
        val foreground: ColorPalette
        val background: ColorPalette
        val overlay: Color
        val success: Color
        val error: Color
    }
}

@Composable
internal fun Web3ModalTheme.Mode.isDarkMode() = when (this) {
    Web3ModalTheme.Mode.LIGHT -> false
    Web3ModalTheme.Mode.DARK -> true
    Web3ModalTheme.Mode.AUTO -> isSystemInDarkTheme()
}

private class CustomWeb3ModalColor(
    override val main100: Color,
    override val main90: Color,
    override val main20: Color,
    override val foreground: ColorPalette,
    override val background: ColorPalette,
    override val overlay: Color,
    override val success: Color,
    override val error: Color
) : Web3ModalTheme.Colors
