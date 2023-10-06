package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun LoadingSpinner(
    strokeWidth: Dp = 4.dp,
    size: Dp = 24.dp,
    tint: Color = Web3ModalTheme.colors.main100
) {
    CircularProgressIndicator(
        strokeWidth = strokeWidth,
        color = tint,
        modifier = Modifier.size(size)
    )
}

@Composable
internal fun LoadingBorder(
    cornerRadius: Dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .drawAnimatedBorder(strokeWidth = 4.dp, shape = RoundedCornerShape(cornerRadius + (cornerRadius/4)), durationMillis = 1700)
    ) {
        Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(cornerRadius),
        ) {
            content()
        }
    }
}

private fun Modifier.drawAnimatedBorder(
    strokeWidth: Dp,
    shape: Shape,
    durationMillis: Int
) = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )
    val brush = Brush.horizontalGradient(
        listOf(
            Web3ModalTheme.colors.main100,
            Web3ModalTheme.colors.main100,
            Web3ModalTheme.colors.main100,
            Color.Transparent, Color.Transparent, Color.Transparent,
            Color.Transparent, Color.Transparent, Color.Transparent,
            Color.Transparent, Color.Transparent, Color.Transparent,
        ))

    Modifier
        .clip(shape)
        .drawWithCache {
            val strokeWidthPx = strokeWidth.toPx()
            val outline: Outline = shape.createOutline(size, layoutDirection, this)
            onDrawWithContent {
                drawContent()
                with(drawContext.canvas.nativeCanvas) {
                    val checkPoint = saveLayer(null, null)
                    drawOutline(
                        outline = outline,
                        color = Color.Gray,
                        style = Stroke(strokeWidthPx * 2)
                    )
                    rotate(angle) {
                        drawCircle(
                            brush = brush,
                            radius = size.width,
                            blendMode = BlendMode.SrcIn,
                        )
                    }
                    restoreToCount(checkPoint)
                }
            }
        }
        .padding(10.dp)
}

@UiModePreview
@Composable
private fun PreviewLoaders() {
    MultipleComponentsPreview(
        { LoadingSpinner() },
        {
            LoadingBorder(20.dp) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color.Blue)
                )
            }
        }
    )
}
