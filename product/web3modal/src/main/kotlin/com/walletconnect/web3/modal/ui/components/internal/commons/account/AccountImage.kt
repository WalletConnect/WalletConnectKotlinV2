package com.walletconnect.web3.modal.ui.components.internal.commons.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import kotlin.math.roundToInt

@Composable
internal fun AccountImage(address: String) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .border(width = 8.dp, color = Web3ModalTheme.colors.overlay05, shape = CircleShape)
            .padding(8.dp)
            .background(brush = Brush.linearGradient(generateAvatarColors(address)), shape = CircleShape)
    )
}

fun generateAvatarColors(address: String): List<Color> {
    val hash = address.lowercase().replace("^0x".toRegex(), "")
    val baseColor = hash.substring(0, 6)
    val rgbColor = hexToRgb(baseColor)
    val colors: MutableList<Color> = mutableListOf()
    for (i in 0 until 5) {
        val tintedColor = tintColor(rgbColor, 0.15 * i)
        colors.add(Color(tintedColor.first, tintedColor.second, tintedColor.third))
    }
    return colors
}

fun hexToRgb(hex: String): Triple<Int, Int, Int> {
    val bigint = hex.toLong(16)
    val r = (bigint shr 16 and 255).toInt()
    val g = (bigint shr 8 and 255).toInt()
    val b = (bigint and 255).toInt()
    return Triple(r, g, b)
}

fun tintColor(rgb: Triple<Int, Int, Int>, tint: Double): Triple<Int, Int, Int> {
    val (r, g, b) = rgb
    val tintedR = (r + (255 - r) * tint).roundToInt()
    val tintedG = (g + (255 - g) * tint).roundToInt()
    val tintedB = (b + (255 - b) * tint).roundToInt()
    return Triple(tintedR, tintedG, tintedB)
}

@UiModePreview
@Composable
private fun AddressImagePreview() {
    ComponentPreview {
        AccountImage("0x59eAF7DD5a2f5e433083D8BbC8de3439542579cb")
    }
}
