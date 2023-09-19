package com.walletconnect.sample.wallet.ui.common.generated

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CancelButton(modifier: Modifier = Modifier) {
    CancelButtonTopLevel(modifier = modifier) {
        Cancel()
    }
}

@Composable
fun Cancel(modifier: Modifier = Modifier) {
    Text(
        text = "Cancel",
        style = TextStyle(
            fontSize = 20.0.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF000000),
        ),
        modifier = modifier.wrapContentHeight(align = Alignment.CenterVertically)
    )
}

@Composable
fun CancelButtonTopLevel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(
                PaddingValues(
                    start = 8.0.dp,
                    top = 0.0.dp,
                    end = 8.0.dp,
                    bottom = 1.0.dp
                )
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFD6D6D6))

    ) {
        content()
    }
}
