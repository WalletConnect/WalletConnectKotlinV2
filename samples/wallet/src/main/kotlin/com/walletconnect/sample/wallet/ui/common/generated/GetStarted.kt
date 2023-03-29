package com.walletconnect.sample.wallet.ui.common.generated

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun GetStarted(modifier: Modifier = Modifier) {
    GetStartedLargeButton(modifier = modifier) {
        GetStartedLabel()
    }
}


@Composable
fun GetStartedLabel() {
    Text(
        text = "Get Started",
        style = TextStyle(
            textAlign = TextAlign.Center,
            fontSize = 20.0.sp,
            color = Color(
                alpha = 255,
                red = 255,
                green = 255,
                blue = 255
            ),
            lineHeight = 1.2.em,
            fontWeight = FontWeight(600.0.toInt()),
        ),
    )
}

@Composable
fun GetStartedLargeButton(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawWithContent(
                onDraw = {
                    drawRect(
                        brush = Brush.linearGradient(
                            0.0f to Color(
                                alpha = 255,
                                red = 51,
                                green = 150,
                                blue = 255
                            ),
                            Float.POSITIVE_INFINITY to Color(
                                alpha = 255,
                                red = 12,
                                green = 124,
                                blue = 242
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
            .requiredHeight(56.0.dp),
        contentAlignment = Alignment.Center
    ) { content() }
}
