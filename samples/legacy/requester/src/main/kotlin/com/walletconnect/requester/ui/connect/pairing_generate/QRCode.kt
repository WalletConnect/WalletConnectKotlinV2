package com.walletconnect.requester.ui.connect.pairing_generate

import androidx.annotation.FloatRange
import com.github.alexzhirkevich.customqrgenerator.createQrOptions
import com.github.alexzhirkevich.customqrgenerator.style.*
import com.walletconnect.requester.R
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

val qrOptions = createQrOptions(1024, 1024, .1f) {
    background {
        color = QrColor.Solid(Color(0xff141414)) // R.color.background
    }

    colors {
        dark = QrColor.Solid(0xffffffff.toColor())
    }

    shapes {
        frame = QrFrameShape.RoundCorners(.35f)
        ball = QrBallShape.RoundCorners(.2f)
        darkPixel = HorizontalStripesShape(.83f)
    }

    logo {
        drawable = DrawableSource.Resource(R.drawable.ic_wc_logo_pink)
        padding = QrLogoPadding.Natural(.0f)
        size = .25f
    }
}

val Neighbors.hasHorizontal: Boolean
    get() = left && right

class HorizontalStripesShape(
    @FloatRange(from = .5, to = 1.0)
    private val width: Float = .8f,
    @FloatRange(from = .0, to = .5)
    private val corner: Float = .5f,
) : QrPixelShape {
    override fun invoke(i: Int, j: Int, elementSize: Int, neighbors: Neighbors): Boolean {
        val shape: QrPixelShape = when {
            neighbors.hasHorizontal -> WidthShape(width = width)
            neighbors.left -> RoundCornersWidthShape(corner = corner, width = width, bottomLeft = false, topLeft = false)
            neighbors.right -> RoundCornersWidthShape(corner = corner, width = width, bottomRight = false, topRight = false)
            else -> QrPixelShape.Circle(size = width)
        }
        return shape.invoke(i, j, elementSize, neighbors)
    }
}

class RoundCornersWidthShape(
    @FloatRange(from = 0.0, to = 0.5) private val corner: Float,
    @FloatRange(from = .5, to = 1.0) private val width: Float,
    private val topLeft: Boolean = true,
    private val topRight: Boolean = true,
    private val bottomLeft: Boolean = true,
    private val bottomRight: Boolean = true,
) : QrPixelShape {

    override fun invoke(
        i: Int, j: Int, elementSize: Int,
        neighbors: Neighbors,
    ): Boolean = isRoundDark(
        i = i,
        j = j,
        elementSize = elementSize,
        neighbors = neighbors,
        corner = corner,
        width = width,
        topLeft = topLeft,
        topRight = topRight,
        bottomLeft = bottomLeft,
        bottomRight = bottomRight
    )

    companion object {
        fun isRoundDark(
            i: Int, j: Int,
            elementSize: Int,
            neighbors: Neighbors,
            corner: Float,
            width: Float,
            topLeft: Boolean,
            topRight: Boolean,
            bottomLeft: Boolean,
            bottomRight: Boolean,
        ): Boolean {
            val cornerRadius = (.5f - corner.coerceIn(0f, .5f)) * elementSize
            val center = elementSize / 2f

            val sub = center - cornerRadius
            val sum = center + cornerRadius

            val (x, y) = when {
                topLeft && i < sub && j < sub -> sub to sub
                topRight && i < sub && j > sum -> sub to sum
                bottomLeft && i > sum && j < sub -> sum to sub
                bottomRight && i > sum && j > sum -> sum to sum
                else -> return WidthShape(width).invoke(i, j, elementSize, neighbors)
            }
            return sqrt((x - i).pow(2) + (y - j).pow(2)) < sub * width
        }
    }
}

class WidthShape(
    @FloatRange(from = .5, to = 1.0)
    private val width: Float = 1f,
) : QrPixelShape {
    override fun invoke(i: Int, j: Int, elementSize: Int, neighbors: Neighbors): Boolean {
        val center = elementSize / 2f
        return abs(center - i) < (width * center)
    }
}
