package com.walletconnect.web3.modal.ui.components.internal.commons.network

import android.graphics.drawable.Drawable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.grayColorFilter
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import java.lang.Float.min
import kotlin.math.sqrt

@Composable
internal fun CircleNetworkImage(
    url: String,
    isEnabled: Boolean = true,
    placeholder: Drawable? = null
) {
    Box(
        modifier = Modifier
            .border(width = 2.dp, color = Web3ModalTheme.colors.overlay05, shape = CircleShape)
            .padding(2.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .placeholder(placeholder)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            colorFilter = if (isEnabled) null else grayColorFilter
        )
    }
}

@Composable
internal fun HexagonNetworkImage(
    url: String,
    isEnabled: Boolean,
    placeholder: Drawable? = null
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .placeholder(placeholder)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = Modifier
            .graphicsLayer {
                shape = HexagonShape
                clip = true
            },
        colorFilter = if (isEnabled) null else grayColorFilter
    )
}

internal val HexagonShape = object: Shape {

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
        val radius = min(size.width / 2f, size.height / 2f)
        customHexagon(radius, size)
    }
}

internal fun Path.customHexagon(radius: Float, size: Size) {
    val triangleHeight = (sqrt(3.0) * radius / 2)
    val centerX = size.width / 2
    val centerY = size.height / 2

    moveTo(centerX, centerY + radius)
    lineTo((centerX - triangleHeight).toFloat(), centerY + radius/2)
    lineTo((centerX - triangleHeight).toFloat(), centerY - radius/2)
    lineTo(centerX, centerY - radius)
    lineTo((centerX + triangleHeight).toFloat(), centerY - radius/2)
    lineTo((centerX + triangleHeight).toFloat(), centerY + radius/2)

    close()
}

@Composable
@UiModePreview
private fun HexagonNetworkImagePreview() {
    MultipleComponentsPreview(
        { HexagonNetworkImage(url = "", isEnabled = true, placeholder = ContextCompat.getDrawable(LocalContext.current, R.drawable.defi)) },
        { HexagonNetworkImage(url = "", isEnabled = false, placeholder = ContextCompat.getDrawable(LocalContext.current, R.drawable.defi)) },
    )
}

@Composable
@UiModePreview
private fun CircleNetworkImagePreview() {
    MultipleComponentsPreview(
        { CircleNetworkImage(url = "", isEnabled = true, placeholder = ContextCompat.getDrawable(LocalContext.current, R.drawable.defi)) },
        { CircleNetworkImage(url = "", isEnabled = false, placeholder = ContextCompat.getDrawable(LocalContext.current, R.drawable.defi)) },
    )
}
