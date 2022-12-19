package com.walletconnect.showcase.ui.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun themedColor(darkColor: Color, lightColor: Color): Color =
    if (isSystemInDarkTheme()) darkColor else lightColor

@Composable
fun themedColor(darkColor: Long, lightColor: Long): Color =
    if (isSystemInDarkTheme()) Color(darkColor) else Color(lightColor)

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

