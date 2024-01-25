package com.walletconnect.sample.wallet.ui.common.generated

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ButtonWithLoader(
    buttonColor: Color,
    loaderColor: Color,
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    content: @Composable () -> Unit
) {
    ButtonTopLevel(buttonColor, modifier = modifier) {
        Button(isLoading = isLoading, content = content, loaderColor = loaderColor)
    }
}

@Composable
fun Button(modifier: Modifier = Modifier, isLoading: Boolean, content: @Composable () -> Unit, loaderColor: Color) {
    AnimatedContent(targetState = isLoading, label = "Loading") { state ->
        if (state) {
            CircularProgressIndicator(
                modifier = modifier
                    .size(48.dp)
                    .padding(8.dp)
                    .wrapContentWidth(align = Alignment.CenterHorizontally)
                    .wrapContentHeight(align = Alignment.CenterVertically),
                color = loaderColor, strokeWidth = 4.dp
            )
        } else {
            content()
        }
    }
}

@Composable
fun ButtonTopLevel(
    buttonColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(
                start = 8.0.dp,
                top = 0.0.dp,
                end = 8.0.dp,
                bottom = 1.0.dp
            )
            .clip(RoundedCornerShape(20.dp))
            .background(buttonColor)
            .fillMaxWidth(1.0f)
            .fillMaxHeight(1.0f)
    ) {
        content()
    }
}
