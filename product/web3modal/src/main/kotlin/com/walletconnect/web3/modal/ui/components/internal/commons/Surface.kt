package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@Composable
internal fun TransparentSurface(
    shape: Shape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.Transparent
    ) {
        content()
    }
}