package com.walletconnect.modal.utils.theme

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.core.content.res.use

@ColorInt
@SuppressLint("Recycle")
fun Context.themeColor(
    @AttrRes themeAttrId: Int
): ComposeColor {
    return obtainStyledAttributes(
        intArrayOf(themeAttrId)
    ).use {
        it.getColor(0, Color.WHITE)
    }.toComposeColor()
}

fun Int.toComposeColor() = ComposeColor(this)