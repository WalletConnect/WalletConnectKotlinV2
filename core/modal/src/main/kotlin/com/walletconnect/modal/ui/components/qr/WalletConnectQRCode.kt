package com.walletconnect.modal.ui.components.qr

import android.content.Context
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.minus
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.createQrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoPadding
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.walletconnect.modal.ui.ComponentPreview
import com.walletconnect.modalcore.R
import androidx.compose.ui.graphics.Color as ComposeColor

enum class QrCodeType {
    WCM, W3M
}

@Composable
fun WalletConnectQRCode(
    qrData: String,
    primaryColor: ComposeColor,
    logoColor: ComposeColor,
    type: QrCodeType,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val qrDrawable = remember(qrData, context, logoColor, primaryColor) {
        QrCodeDrawable(
            data = QrData.Url(qrData),
            options = createQROptions(context, logoColor, primaryColor, type)
        )
    }

    Image(
        painter = rememberDrawablePainter(drawable = qrDrawable),
        contentDescription = "content description",
        modifier = Modifier
            .aspectRatio(1f)
            .then(modifier)
    )
}

@Preview
@Composable
private fun WalletConnectModalQRCodePreview() {
    ComponentPreview {
        WalletConnectQRCode(
            "Preview qr code data with wallet connect logo inside",
            ComposeColor.White,
            ComposeColor(0xFF3496ff),
            QrCodeType.WCM
        )
    }
}

@Preview
@Composable
private fun Web3ModalQRCodePreview() {
    ComponentPreview {
        WalletConnectQRCode(
            "Preview qr code data with wallet connect logo inside",
            ComposeColor.White,
            ComposeColor(0xFF3496ff),
            QrCodeType.W3M
        )
    }
}

private fun createQROptions(
    context: Context,
    logoColor: Color,
    primaryColor: Color,
    type: QrCodeType,
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
            frame = type.getVectorFrameShape()
            ball = type.getVectorBallShape()
            darkPixel = QrVectorPixelShape.RoundCornersVertical(.95f)
        }
    }

private fun QrCodeType.getVectorFrameShape() = when (this) {
    QrCodeType.WCM -> QrVectorFrameShape.RoundCorners(.4f)
    QrCodeType.W3M -> W3MQrVectorFrameShape
}

private fun QrCodeType.getVectorBallShape() = when (this) {
    QrCodeType.WCM -> QrVectorBallShape.RoundCorners(.2f)
    QrCodeType.W3M -> QrVectorBallShape.RoundCorners(.4f)
}

private object W3MQrVectorFrameShape : QrVectorFrameShape {

    private const val corner: Float = 0.45f
    private const val width = 1f

    override fun createPath(size: Float, neighbors: Neighbors): Path {

        val width = size / 7f * width.coerceAtLeast(0f)

        val outerCornerSize = corner * size
        val innerCornerSize = corner * (size - 2 * width)

        return Path().apply {
            addRoundRect(
                RectF(0f, 0f, size, size),
                floatArrayOf(
                    outerCornerSize,
                    outerCornerSize,
                    outerCornerSize,
                    outerCornerSize,
                    outerCornerSize,
                    outerCornerSize,
                    outerCornerSize,
                    outerCornerSize,
                ),
                Path.Direction.CW
            )
        } - Path().apply {
            addRoundRect(
                RectF(width, width, size - width, size - width),
                floatArrayOf(
                    innerCornerSize,
                    innerCornerSize,
                    innerCornerSize,
                    innerCornerSize,
                    innerCornerSize,
                    innerCornerSize,
                    innerCornerSize,
                    innerCornerSize,
                    innerCornerSize,
                ),
                Path.Direction.CCW
            )
        }
    }

}

