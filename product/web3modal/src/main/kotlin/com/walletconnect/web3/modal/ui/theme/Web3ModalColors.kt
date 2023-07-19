package com.walletconnect.web3.modal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal data class Web3ModalColors(
    val main100: Color,
    val main90: Color,
    val main20: Color,
    val foreground: ColorPalette,
    val background: ColorPalette,
    val success: Color,
    val error: Color,
    val teal: Color,
    val magenta: Color,
    val indigo: Color,
    val orange: Color,
    val purple: Color
) {
    val inverse100 = Color.White
    val inverse000 = Color.Black
    val overlay = Color.White

    val main15: Color = main100.copy(.15f)
    val main10: Color = main100.copy(.1f)
    val main005: Color = main100.copy(.05f)

    val overlay02: Color = overlay.copy(.02f)
    val overlay05: Color = overlay.copy(.05f)
    val overlay10: Color = overlay.copy(.10f)
    val overlay15: Color = overlay.copy(.15f)
    val overlay20: Color = overlay.copy(.2f)
    val overlay25: Color = overlay.copy(.25f)
    val overlay30: Color = overlay.copy(.3f)
}
internal data class ColorPalette(
    val color100: Color,
    val color125: Color,
    val color150: Color,
    val color175: Color,
    val color200: Color,
    val color225: Color,
    val color250: Color,
    val color275: Color,
    val color300: Color,
)
@Composable
internal fun provideWeb3ModalColors(): Web3ModalColors = if (isSystemInDarkTheme()) {
    defaultDarkWeb3ModalColors
} else {
    defaultLightWeb3ModalColors
}

private val defaultDarkWeb3ModalColors = Web3ModalColors(
    main100 = Color(0xFF47A1FF),
    main90 = Color(0xFF59AAFF),
    main20 = Color(0xFF6CB4FF),
    foreground = ColorPalette(
        Color(0xFFE4E7E7),
        Color(0xFFD0D5D5),
        Color(0xFFA8B1B1),
        Color(0xFFA8B0B0),
        Color(0xFF949E9E),
        Color(0xFF868F8F),
        Color(0xFF788080),
        Color(0xFF788181),
        Color(0xFF637777)
    ),
    background = ColorPalette(
        Color(0xFF141414),
        Color(0xFF191A1A),
        Color(0xFF1E1F1F),
        Color(0xFF222525),
        Color(0xFF272A2A),
        Color(0xFF2C3030),
        Color(0xFF313535),
        Color(0xFF363B3B),
        Color(0xFF3B4040)
    ),
    success = Color(0xFF26D962),
    error = Color(0xFFF25A67),
    teal = Color(0xFF36E2E2),
    magenta = Color(0xFFCB4D8C),
    indigo = Color(0xFF516DFB),
    orange = Color(0xFFFFA64C),
    purple = Color(0xFF9063F7),
)

private
val defaultLightWeb3ModalColors = Web3ModalColors(
    main100 = Color(0xFF3396FF),
    main90 = Color(0xFF2D7DD2),
    main20 = Color(0xFF2978CC),
    foreground = ColorPalette(
        Color(0xFF141414),
        Color(0xFF2D23131),
        Color(0xFF474D4D),
        Color(0xFF636D6D),
        Color(0xFF798686),
        Color(0xFF828F8F),
        Color(0xFF8B9797),
        Color(0xFF95A0A0),
        Color(0xFF9EA9A9)
    ),
    background = ColorPalette(
        Color(0xFFFFFFFF),
        Color(0xFFF5FAFA),
        Color(0xFFF3F8F8),
        Color(0xFFEEF4F4),
        Color(0xFFEAF1F1),
        Color(0xFFE5EDED),
        Color(0xFFE1E9E9),
        Color(0xFFDCE7E7),
        Color(0xFFD8E3E3)
    ),
    success = Color(0xFF26B562),
    error = Color(0xFFF05142),
    teal = Color(0xFF2BB6B6),
    magenta = Color(0xFFC65380),
    indigo = Color(0xFF3D5CF5),
    orange = Color(0xFFEA8C2E),
    purple = Color(0xFF794CFF),
)
