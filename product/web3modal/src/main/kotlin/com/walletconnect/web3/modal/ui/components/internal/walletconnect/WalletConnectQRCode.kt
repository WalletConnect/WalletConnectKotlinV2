package com.walletconnect.web3.modal.ui.components.internal.walletconnect

import android.content.Context
import android.graphics.Path
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.style.*
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.createQrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.*
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import androidx.compose.ui.graphics.Color as ComposeColor


@Composable
internal fun WalletConnectQRCode(
    qrData: String,
    primaryColor: ComposeColor,
    logoColor: ComposeColor,
) {
    val context = LocalContext.current
    val qrDrawable =
        QrCodeDrawable(
            data = QrData.Url(qrData),
            options = createQROptions(context, logoColor, primaryColor)
        )

    Image(
        painter = rememberDrawablePainter(drawable = qrDrawable),
        contentDescription = "content description",
        modifier = Modifier
            .padding(10.dp)
            .aspectRatio(1f)
    )
}

@Preview
@Composable
private fun QRCodePreview() {
    ComponentPreview {
        WalletConnectQRCode(
            "Preview qr code data with wallet connect logo inside",
            ComposeColor.White,
            ComposeColor(0xFF3496ff)
        )
    }
}

private fun createQROptions(
    context: Context,
    logoColor: ComposeColor,
    primaryColor: ComposeColor,
) =
    createQrVectorOptions {
        padding = .0f

        logo {
            val rawDrawable = ContextCompat.getDrawable(context, R.drawable.ic_wallet_connect_qr_logo)
            val wrappedDrawable = DrawableCompat.wrap(rawDrawable!!)
            DrawableCompat.setTint(wrappedDrawable, logoColor.toArgb())
            drawable = wrappedDrawable
            size = .2f
            padding = QrVectorLogoPadding.Natural(.2f)
        }

        colors {
            dark = QrVectorColor.Solid(primaryColor.toArgb())
        }
        shapes {
            frame = QrVectorFrameShape.RoundCorners(.4f)
            ball = QrVectorBallShape.RoundCorners(.2f)
            darkPixel = VerticalStripesShape()
        }
    }

private val Neighbors.hasVertical: Boolean
    get() = top && bottom

private class VerticalStripesShape() : QrVectorPixelShape {
    override fun createPath(size: Float, neighbors: Neighbors): Path {
        val shape: QrVectorPixelShape = when {
            neighbors.hasVertical -> DefaultVectorShape()
            neighbors.top -> RoundTopCornersVerticalVectorShape()
            neighbors.bottom -> RoundBottomCornersVerticalVectorShape()
            else -> CircleVectorShape()
        }
        return shape.createPath(size, neighbors)
    }
}

private class CircleVectorShape : QrVectorPixelShape, QrVectorShapeModifier {
    override fun createPath(size: Float, neighbors: Neighbors) = Path().apply {
        addCircle(size/2f, size/2f, size/2 * 0.9f, Path.Direction.CW)
    }
}

private class DefaultVectorShape : QrVectorPixelShape, QrVectorShapeModifier {
    override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {
        val padding = size * .95f
        addRect(padding, 0f, size - padding, size, Path.Direction.CW)
    }
}
private class RoundTopCornersVerticalVectorShape : QrVectorPixelShape, QrVectorShapeModifier {
    override fun createPath(size: Float, neighbors: Neighbors) = Path().apply {
        val padding = .05f
        addRect((size*padding), 0f, size - (size*padding), size / 2f, Path.Direction.CW)
        addCircle(size / 2, size / 2, (size / 2f) * 0.9f , Path.Direction.CW)
    }
}

private class RoundBottomCornersVerticalVectorShape : QrVectorPixelShape, QrVectorShapeModifier {
    override fun createPath(size: Float, neighbors: Neighbors) = Path().apply {
        val padding = .05f
        addRect((size*padding), size / 2f, size - (size*padding), size, Path.Direction.CW)
        addCircle(size / 2, size / 2, (size / 2f) * 0.9f , Path.Direction.CW)
    }
}
