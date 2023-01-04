package com.walletconnect.web3.wallet.ui.common.generated

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun AllowButton(modifier: Modifier = Modifier) {
    AllowButtonTopLevel(modifier = modifier) {
        Allow()
    }
}

@Composable
fun Allow(modifier: Modifier = Modifier) {
    Text(
        text = "Allow",
        style = TextStyle(
            fontSize = 20.0.sp,
            color = Color(
                alpha = 255,
                red = 255,
                green = 255,
                blue = 255
            ),
            lineHeight = 1.2.em,
            letterSpacing = 0.3799999952316284.sp,
            fontWeight = FontWeight(600.0.toInt()),
        ),
        modifier = modifier.wrapContentHeight(align = Alignment.CenterVertically)
    )
}

@Composable
fun AllowButtonTopLevel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .padding(
                start = 16.0.dp,
                top = 0.0.dp,
                end = 16.0.dp,
                bottom = 1.0.dp
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Transparent)
            .drawWithContent(
                onDraw = {
                    drawRect(
                        brush = Brush.linearGradient(
                            0.0f to Color(
                                alpha = 255,
                                red = 42,
                                green = 237,
                                blue = 107
                            ),
                            Float.POSITIVE_INFINITY to Color(
                                alpha = 255,
                                red = 28,
                                green = 200,
                                blue = 86
                            ),
                            start = Offset(
                                0.5f,
                                0.0f
                            ),
                            end = Offset(
                                0.5f,
                                Float.POSITIVE_INFINITY
                            )
                        )
                    )
                    drawContent()
                }
            )
            .fillMaxWidth(1.0f)
            .fillMaxHeight(1.0f)
    ) {
        content()
    }
}
