package com.walletconnect.modalcore.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
internal fun ComponentPreview(
    color: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.background(color)) {
        content()
    }
}