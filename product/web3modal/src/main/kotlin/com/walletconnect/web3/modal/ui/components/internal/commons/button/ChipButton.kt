package com.walletconnect.web3.modal.ui.components.internal.commons.button

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    StyledButton(
        style = style,
        size = size,
        isEnabled = isEnabled,
        onClick = onClick
    ) { buttonData ->
        startIcon(buttonData.tint)
        HorizontalSpacer(width = 4.dp)
        Text(text = text, style = buttonData.textStyle)
        HorizontalSpacer(width = 4.dp)
        endIcon(buttonData.tint)
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

