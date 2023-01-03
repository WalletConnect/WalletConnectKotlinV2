package com.walletconnect.web3.wallet.ui.routes.composable_routes.connections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.relay.compose.RelayContainer
import com.walletconnect.web3.wallet.sample.R


/**
 * This composable was generated from the UI Package 'paste_button'.
 * Szymon: However there were import issues so it's removed from generated source.
 * Also modified some code
 */
@Composable
fun ConnectionsButton(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center, modifier = modifier
            .size(56.dp)
            .shadow(elevation = 4.dp, shape = CircleShape)
            .background(Color.Transparent)
            .drawWithContent(onDraw = {
                drawRect(
                    brush = Brush.linearGradient(
                        0.0f to Color(
                            alpha = 255, red = 51, green = 150, blue = 255
                        ), Float.POSITIVE_INFINITY to Color(
                            alpha = 255, red = 12, green = 124, blue = 242
                        ), start = Offset.Zero, end = Offset.Infinite
                    )
                )
                drawContent()
            })
    ) {
        content()
    }
}

@Preview(widthDp = 56, heightDp = 56)
@Composable
private fun PasteButtonPreview() {
    MaterialTheme {
        RelayContainer {
            ConnectionsButton(
                content = {
                    Icon(ImageVector.vectorResource(id = R.drawable.paste_icon), contentDescription = "Scan QRCode Icon", tint = Color(0xFFFFFFFF))
                }, modifier = Modifier
                    .rowWeight(1.0f)
                    .columnWeight(1.0f)
            )
        }
    }
}