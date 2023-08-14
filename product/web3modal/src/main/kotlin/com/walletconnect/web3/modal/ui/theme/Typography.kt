package com.walletconnect.web3.modal.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp

internal val defaultTypography = Web3ModalTypography(
    title500 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        letterSpacing = TextUnit(-.04f, TextUnitType.Sp)
    ),
    title600 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        letterSpacing = TextUnit(-.04f, TextUnitType.Sp)
    ),
    title700 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = TextUnit(-.04f, TextUnitType.Sp)
    ),
    large500 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        letterSpacing = TextUnit(-.04f, TextUnitType.Sp)
    ),
    large600 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        letterSpacing = TextUnit(-.04f, TextUnitType.Sp)
    ),
    large700 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = TextUnit(-.04f, TextUnitType.Sp)
    ),
    paragraph500 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = TextUnit(-.04f, TextUnitType.Sp)
    ),
    paragraph600 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = TextUnit(-.04f, TextUnitType.Sp)
    ),
    paragraph700 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = TextUnit(-.04f, TextUnitType.Sp)
    ),
    small500 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = TextUnit(-.04f, TextUnitType.Sp)
    ),
    small600 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = TextUnit(-.04f, TextUnitType.Sp)
    ),
    tiny500 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = TextUnit(-.04f, TextUnitType.Sp)
    ),
    tiny600 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = TextUnit(-.04f, TextUnitType.Sp)
    ),
    micro600 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = TextUnit(.02f, TextUnitType.Sp)
    ),
    micro700 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = TextUnit(.02f, TextUnitType.Sp)
    ),
)

internal data class Web3ModalTypography(
    val title500: TextStyle,
    val title600: TextStyle,
    val title700: TextStyle,
    val large500: TextStyle,
    val large600: TextStyle,
    val large700: TextStyle,
    val paragraph500: TextStyle,
    val paragraph600: TextStyle,
    val paragraph700: TextStyle,
    val small500: TextStyle,
    val small600: TextStyle,
    val tiny500: TextStyle,
    val tiny600: TextStyle,
    val micro600: TextStyle,
    val micro700: TextStyle,
)