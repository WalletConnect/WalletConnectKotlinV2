package com.walletconnect.web3.modal.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal fun provideDefaultTypography(colors: Web3ModalColors) = Web3ModalTypography(
    mediumTitle400 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        color = colors.foreground.color100
    ),
    mediumTitle500 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        color = colors.foreground.color100
    ),
    mediumTitle600 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        color = colors.foreground.color100
    ),
    smallTitle400 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        color = colors.foreground.color100
    ),
    smallTitle500 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        color = colors.foreground.color100
    ),
    smallTitle600 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        color = colors.foreground.color100
    ),
    large400 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        color = colors.foreground.color100
    ),
    large500 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        color = colors.foreground.color100
    ),
    large600 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        color = colors.foreground.color100
    ),
    medium400 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = colors.foreground.color100
    ),
    medium500 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = colors.foreground.color100
    ),
    medium600 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = colors.foreground.color100
    ),
    paragraph400 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = colors.foreground.color100
    ),
    paragraph500 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = colors.foreground.color100
    ),
    paragraph600 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = colors.foreground.color100
    ),
    small400 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = colors.foreground.color100
    ),
    small500 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = colors.foreground.color100
    ),
    small600 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = colors.foreground.color100
    ),
    tiny400 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = colors.foreground.color100
    ),
    tiny500 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = colors.foreground.color100
    ),
    tiny600 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = colors.foreground.color100
    ),
    micro600 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        color = colors.foreground.color100
    ),
    micro700 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        color = colors.foreground.color100
    ),
)

internal data class Web3ModalTypography(
    val mediumTitle400: TextStyle,
    val mediumTitle500: TextStyle,
    val mediumTitle600: TextStyle,
    val smallTitle400: TextStyle,
    val smallTitle500: TextStyle,
    val smallTitle600: TextStyle,
    val large400: TextStyle,
    val large500: TextStyle,
    val large600: TextStyle,
    val medium400: TextStyle,
    val medium500: TextStyle,
    val medium600: TextStyle,
    val paragraph400: TextStyle,
    val paragraph500: TextStyle,
    val paragraph600: TextStyle,
    val small400: TextStyle,
    val small500: TextStyle,
    val small600: TextStyle,
    val tiny400: TextStyle,
    val tiny500: TextStyle,
    val tiny600: TextStyle,
    val micro600: TextStyle,
    val micro700: TextStyle,
)