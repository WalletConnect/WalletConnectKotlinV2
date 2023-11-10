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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.network.HexagonShape
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun LoadingSpinner(
    strokeWidth: Dp = 4.dp,
    size: Dp = 24.dp,
    tint: Color = Web3ModalTheme.colors.accent100
) {
    CircularProgressIndicator(
        strokeWidth = strokeWidth,
        color = tint,
        modifier = Modifier.size(size)
    )
}

@Composable
internal fun LoadingHexagonBorder(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .drawAnimatedBorder(strokeWidth = 4.dp, progress = .25f, shape = HexagonShape, durationMillis = 1000)
    ) {
        Surface(
            color = Color.Transparent,
            shape = HexagonShape,
        ) {
            content()
        }
    }
}

@Composable
internal fun LoadingBorder(
    cornerRadius: Dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .drawAnimatedBorder(strokeWidth = 4.dp, progress = .25f, shape = RoundedCornerShape(cornerRadius + (cornerRadius / 4)), durationMillis = 1000)
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
    progress: Float,
    shape: Shape,
    durationMillis: Int
) = composed {
    val loaderColor = Web3ModalTheme.colors.accent100
    var pathLenght by remember { mutableFloatStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val loaderProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = pathLenght,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    Modifier
        .clip(shape)
        .padding(strokeWidth)
        .drawWithCache {
            val strokeWidthPx = strokeWidth.toPx()
            val outline: Outline = shape.createOutline(size, layoutDirection, this)
            onDrawWithContent {
                drawContent()
                with(drawContext.canvas.nativeCanvas) {
                    val checkPoint = saveLayer(null, null)
                    val path = Path().apply { addOutline(outline) }
                    val pathMeasure = PathMeasure().apply { setPath(path, false) }
                    pathLenght = pathMeasure.length
                    drawPath(
                        path = path,
                        color = loaderColor,
                        style = Stroke(
                            width = strokeWidthPx,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(0f, (1f - progress) * pathMeasure.length, progress * pathMeasure.length, 0f), loaderProgress),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                    restoreToCount(checkPoint)
                }
            }
        }
        .padding(8.dp)
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
        },
        {
            LoadingHexagonBorder {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color.Blue)
                )
            }
        }
    )
}
