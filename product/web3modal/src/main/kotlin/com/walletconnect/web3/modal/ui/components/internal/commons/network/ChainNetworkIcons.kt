package com.walletconnect.web3.modal.ui.components.internal.commons.network

import android.graphics.drawable.Drawable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import com.walletconnect.web3.modal.utils.grayColorFilter
import com.walletconnect.web3.modal.utils.imageHeaders
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun CircleNetworkImage(
    data: Any,
    isEnabled: Boolean = true,
    size: Dp = 36.dp,
    placeholder: Drawable? = null
) {
    Box(
        modifier = Modifier
            .size(size)
            .border(width = 2.dp, color = Web3ModalTheme.colors.grayGlass05, shape = CircleShape)
            .padding(2.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(data)
                .placeholder(placeholder)
                .crossfade(true)
                .imageHeaders()
                .build(),
            contentDescription = null,
            modifier = Modifier.clip(CircleShape),
            colorFilter = if (isEnabled) null else grayColorFilter
        )
    }
}

@Composable
internal fun HexagonNetworkImage(
    data: Any?,
    isEnabled: Boolean,
    size: Dp = 56.dp,
    borderColor: Color? = null,
    placeholder: Drawable? = null
) {
    val overlayColor = Web3ModalTheme.colors.grayGlass10
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(data)
            .placeholder(placeholder)
            .crossfade(true)
            .imageHeaders()
            .build(),
        contentDescription = null,
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                shape = HexagonShape
                clip = true
            }
            .drawWithContent {
                drawContent()
                drawPath(
                    path = drawCustomHexagonPath(this.size),
                    color = borderColor ?: overlayColor,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    ),
                    blendMode = BlendMode.SrcOver
                )
            },
        colorFilter = if (isEnabled) null else grayColorFilter
    )
}

internal val HexagonShape = object : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(
            path = drawCustomHexagonPath(size)
        )
    }
}

internal fun drawCustomHexagonPath(size: Size): Path {
    return Path().apply {
        customHexagon(size)
    }
}

internal fun Path.customHexagon(size: Size) {


    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = size.minDimension / 2f
    val cornerRadius = 5f

    moveTo(centerX, size.height - cornerRadius)
    for (i in 1..6) {
        val angle = i * 60.0
        val x = centerX + radius * cos(Math.toRadians(angle)).toFloat()
        val y = centerY + radius * sin(Math.toRadians(angle)).toFloat()


        val controlPointX = centerX + (radius - cornerRadius) * cos(Math.toRadians(angle)).toFloat()
        val controlPointY = centerY + (radius - cornerRadius) * sin(Math.toRadians(angle)).toFloat()
        val endPointX = centerX + radius * cos(Math.toRadians(angle - 60)).toFloat()
        val endPointY = centerY + radius * sin(Math.toRadians(angle - 60)).toFloat()

        cubicTo(
            endPointY, endPointX,
            y, x,
            controlPointY, controlPointX,
        )
    }
    close()
}

@Composable
@UiModePreview
private fun HexagonNetworkImagePreview() {
    MultipleComponentsPreview(
        { HexagonNetworkImage(data = "", borderColor = Color.Transparent, isEnabled = true, placeholder = ContextCompat.getDrawable(LocalContext.current, R.drawable.defi)) },
        { HexagonNetworkImage(data = "", borderColor = Color.Transparent, isEnabled = false, placeholder = ContextCompat.getDrawable(LocalContext.current, R.drawable.defi)) },
    )
}

@Composable
@UiModePreview
private fun CircleNetworkImagePreview() {
    MultipleComponentsPreview(
        { CircleNetworkImage(data = "", isEnabled = true, placeholder = ContextCompat.getDrawable(LocalContext.current, R.drawable.defi)) },
        { CircleNetworkImage(data = "", isEnabled = false, placeholder = ContextCompat.getDrawable(LocalContext.current, R.drawable.defi)) },
    )
}
