package com.walletconnect.web3.modal.ui.components.internal.commons.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.CompassIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.ExternalIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.HorizontalSpacer
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview

@Composable
internal fun ChipButton(
    text: String,
    startIcon: @Composable (Color) -> Unit,
    endIcon: @Composable (Color) -> Unit,
    style: ButtonStyle,
    size: ButtonSize,
    paddingValues: PaddingValues? = null,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val backgroundColor = style.getBackgroundColor(isEnabled)
    val borderColor = style.getBorder(isEnabled)
    val tint = style.getTextColor(isEnabled)
    val textStyle = size.getTextStyle().copy(color = tint)
    val isClickEnabled = isEnabled && style != ButtonStyle.LOADING
    val height = size.getHeight()

    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(100)
    ) {
        Row(
            modifier = Modifier
                .height(height)
                .clickable(isClickEnabled) { onClick() }
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(100))
                .background(backgroundColor)
                .padding(paddingValues ?: size.getContentPadding()),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            startIcon(tint)
            HorizontalSpacer(width = 4.dp)
            Text(text = text, style = textStyle)
            HorizontalSpacer(width = 4.dp)
            endIcon(tint)
        }
    }
}

@Composable
@UiModePreview
private fun PreviewChipButton(
    @PreviewParameter(ChipButtonPreviewProvider::class) data: ButtonPreview
) {
    ComponentPreview {
        ChipButton(
            text = "Chip Button",
            startIcon = { CompassIcon(it) },
            endIcon = { ExternalIcon(it) },
            style = data.style,
            size = data.size,
            isEnabled = data.isEnabled,
            onClick = {}
        )
    }
}

internal class ChipButtonPreviewProvider : PreviewParameterProvider<ButtonPreview> {
    override val values = sequenceOf(
        ButtonPreview(ButtonStyle.MAIN, ButtonSize.M, true),
        ButtonPreview(ButtonStyle.MAIN, ButtonSize.M, false),
        ButtonPreview(ButtonStyle.MAIN, ButtonSize.S, true),
        ButtonPreview(ButtonStyle.MAIN, ButtonSize.S, false),
        ButtonPreview(ButtonStyle.SHADE, ButtonSize.M, true),
        ButtonPreview(ButtonStyle.SHADE, ButtonSize.M, false),
        ButtonPreview(ButtonStyle.SHADE, ButtonSize.S, true),
        ButtonPreview(ButtonStyle.SHADE, ButtonSize.S, false),
    )
}

