package com.walletconnect.web3.modal.ui.components.internal.commons.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.HorizontalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.RetryIcon
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview

@Composable
internal fun TryAgainButton(
    style: ButtonStyle = ButtonStyle.ACCENT,
    size: ButtonSize = ButtonSize.M,
    onClick: () -> Unit
) {
    ImageButton(
        text = "Try again",
        image = { RetryIcon(it) },
        style = style,
        size = size,
        onClick = onClick
    )
}

@Composable
internal fun ImageButton(
    text: String,
    image: @Composable (Color) -> Unit,
    style: ButtonStyle,
    size: ButtonSize,
    isEnabled: Boolean = true,
    onClick: () -> Unit,
) {
    StyledButton(style = style, size = size, isEnabled = isEnabled, onClick = onClick) {
        image(it.tint)
        HorizontalSpacer(width = 4.dp)
        Text(text = text, style = it.textStyle)
    }
}

@Composable
internal fun TextButton(
    text: String,
    style: ButtonStyle,
    size: ButtonSize,
    isEnabled: Boolean = true,
    onClick: () -> Unit,
) {
    StyledButton(
        style = style,
        size = size,
        isEnabled = isEnabled,
        onClick = onClick
    ) {
        Text(text = text, style = it.textStyle)
    }
}

@Composable
private fun StyledButton(
    style: ButtonStyle,
    size: ButtonSize,
    isEnabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable RowScope.(ButtonData) -> Unit
) {
    val backgroundColor = style.getBackgroundColor(isEnabled)
    val borderColor = style.getBorder(isEnabled)
    val tint = style.getTextColor(isEnabled)
    val textStyle = size.getTextStyle().copy(color = tint)
    val padding = size.getContentPadding()

    RoundedButton(
        modifier = Modifier
            .clickable(enabled = isEnabled, onClick = onClick)
            .background(backgroundColor)
            .border(color = borderColor, width = 1.dp, shape = RoundedCornerShape(100))
            .padding(paddingValues = padding)
    ) {
        content(ButtonData(size, style, textStyle, tint, backgroundColor))
    }
}

@Composable
private fun RoundedButton(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(100)
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
@UiModePreview
private fun PreviewButtons() {
    MultipleComponentsPreview(
        { TryAgainButton {} },
    )
}

@Composable
@UiModePreview
private fun PreviewTextButton() {
    MultipleComponentsPreview(
        { TextButton(text = "Button", style = ButtonStyle.MAIN, size = ButtonSize.S) {} },
        { TextButton(text = "Button", style = ButtonStyle.MAIN, size = ButtonSize.S, isEnabled = false) {} },
        { TextButton(text = "Button", style = ButtonStyle.MAIN, size = ButtonSize.M) {} },
        { TextButton(text = "Button", style = ButtonStyle.MAIN, size = ButtonSize.M, isEnabled = false) {} },
        { TextButton(text = "Button", style = ButtonStyle.ACCENT, size = ButtonSize.S) {} },
        { TextButton(text = "Button", style = ButtonStyle.ACCENT, size = ButtonSize.S, isEnabled = false) {} },
        { TextButton(text = "Button", style = ButtonStyle.ACCENT, size = ButtonSize.M) {} },
        { TextButton(text = "Button", style = ButtonStyle.ACCENT, size = ButtonSize.M, isEnabled = false) {} },
    )
}
