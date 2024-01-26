package com.walletconnect.sample.common.ui.commons

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BlueButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = Color(0xFFE5E7E7)
    Button(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF3496ff),
            contentColor = contentColor
        ),
        onClick = {
            onClick()
        },
    ) {
        Text(text = text, color = contentColor)
    }
}

@Composable
fun ButtonWithLoader(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean,
) {
    val contentColor = Color(0xFFE5E7E7)
    AnimatedContent(targetState = isLoading, label = "Loading") { state ->
        if (state) {
            CircularProgressIndicator(
                modifier = modifier
                    .size(48.dp)
                    .padding(8.dp)
                    .wrapContentWidth(align = Alignment.CenterHorizontally)
                    .wrapContentHeight(align = Alignment.CenterVertically),
                color = contentColor, strokeWidth = 4.dp
            )
        } else {
            Button(
                shape = RoundedCornerShape(12.dp),
                modifier = modifier,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF3496ff),
                    contentColor = contentColor
                ),
                onClick = {
                    onClick()
                },
            ) {
                Text(text = text, color = contentColor, style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight(400),
                ),)
            }
        }
    }
}