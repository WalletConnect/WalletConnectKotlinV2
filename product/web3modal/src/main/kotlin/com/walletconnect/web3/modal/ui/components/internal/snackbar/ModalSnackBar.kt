package com.walletconnect.web3.modal.ui.components.internal.snackbar

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.HorizontalSpacer
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun ModalSnackBar(
    snackBarEvent: SnackBarEvent?
) {
    val shape = RoundedCornerShape(100)
    Row(
        modifier = Modifier
            .background(
                color = Web3ModalTheme.colors.background.color175,
                shape = shape
            )
            .border(
                width = 1.dp,
                color = Web3ModalTheme.colors.overlay05,
                shape = shape
            )
            .padding(8.dp)
            .animateContentSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        snackBarEvent?.let {
            val icon = when (snackBarEvent.type) {
                SnackBarEventType.SUCCESS -> R.drawable.ic_success
                SnackBarEventType.INFO -> R.drawable.ic_info
                SnackBarEventType.ERROR -> R.drawable.ic_error
            }
            val tint = when (snackBarEvent.type) {
                SnackBarEventType.SUCCESS -> Web3ModalTheme.colors.success
                SnackBarEventType.INFO -> Web3ModalTheme.colors.foreground.color200
                SnackBarEventType.ERROR -> Web3ModalTheme.colors.error
            }

            Icon(
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = "SnackBar icon",
                tint = tint
            )
            HorizontalSpacer(width = 8.dp)
            Text(
                text = snackBarEvent.message,
                style = Web3ModalTheme.typo.paragraph500
            )
            HorizontalSpacer(width = 8.dp)
        }
    }
}


@UiModePreview
@Composable
private fun ModalSnackBarPreview(
    @PreviewParameter(ModalSnackBarEventProvider::class) event: SnackBarEvent
) {
    ComponentPreview {
        ModalSnackBar(snackBarEvent = event)
    }
}

private class ModalSnackBarEventProvider : PreviewParameterProvider<SnackBarEvent> {

    private class Event(
        override val type: SnackBarEventType,
        override val message: String,
        override val duration: SnackbarDuration
    ) : SnackBarEvent {
        override fun dismiss() {}
    }

    override val values: Sequence<SnackBarEvent>
        get() = sequenceOf(
            Event(SnackBarEventType.SUCCESS, "Address copied", SnackbarDuration.SHORT),
            Event(SnackBarEventType.INFO, "Signature canceled", SnackbarDuration.SHORT),
            Event(SnackBarEventType.ERROR, "Network error", SnackbarDuration.SHORT),
        )
}
