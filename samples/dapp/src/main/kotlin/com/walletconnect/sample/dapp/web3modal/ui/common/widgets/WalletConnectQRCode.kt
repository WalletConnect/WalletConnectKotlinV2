package com.walletconnect.sample.dapp.web3modal.ui.common.widgets

import android.content.Context
import android.graphics.Path
import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.style.*
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.createQrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.*
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.walletconnect.sample.dapp.R
import com.walletconnect.sample_common.ui.theme.PreviewTheme
import androidx.compose.ui.graphics.Color as ComposeColor


@Composable
fun WalletConnectQRCode(
    qrData: String,
    primaryColor: ComposeColor,
    logoColor: ComposeColor,
) {
    val context = LocalContext.current
    val qrDrawable =
        QrCodeDrawable(data = QrData.Url(qrData), options = createQROptions(context, logoColor, primaryColor))

    Image(
        painter = rememberDrawablePainter(drawable = qrDrawable),
        contentDescription = "content description",
        modifier = Modifier.aspectRatio(1f)
    )
}

@Preview
@Composable
private fun QRCodePreview() {
    PreviewTheme {
        WalletConnectQRCode(
            "Preview qr code data with wallet connect logo inside",
            ComposeColor.White,
            ComposeColor(0xFF3496ff))
    }
}

fun createQROptions(
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
            size = .25f
            padding = QrVectorLogoPadding.Natural(0.25f)
        }

        colors {
            dark = QrVectorColor.Solid(primaryColor.toArgb())
        }
        shapes {
            shapes {
                frame = QrVectorFrameShape.RoundCorners(.35f)
                ball = QrVectorBallShape.RoundCorners(.2f)
                darkPixel = VerticalStripesShape()
            }
        }
    }

val Neighbors.hasVertical: Boolean
    get() = top && bottom


class VerticalStripesShape() : QrVectorPixelShape {
    override fun createPath(size: Float, neighbors: Neighbors): Path {
        val shape: QrVectorPixelShape = when {
            neighbors.hasVertical -> QrVectorPixelShape.Default
            neighbors.top -> QrVectorPixelShape.RoundCornersVertical(size)
            neighbors.bottom -> QrVectorPixelShape.RoundCornersVertical(size)
            else -> QrVectorPixelShape.Circle(size = size)
        }
        return shape.createPath(size, neighbors)
    }
}

