package com.walletconnect.sample.common.ui.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun FullScreenLoader(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF3496ff),
    backgroundColor: Color = MaterialTheme.colors.background.copy(alpha = .5f)
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(backgroundColor)
    ) {
        CircularProgressIndicator(
            color = color,
            modifier = modifier
        )
    }
}
