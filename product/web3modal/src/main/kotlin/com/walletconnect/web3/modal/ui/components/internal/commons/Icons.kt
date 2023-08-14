package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.walletconnect.modal.ui.components.common.roundedClickable
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun BackArrowIcon(
    tint: Color = Web3ModalTheme.colors.foreground.color100,
    onClick: () -> Unit
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_chevron_left),
        contentDescription = ContentDescription.BACK_ARROW.description,
        tint = tint,
        modifier = Modifier
            .size(36.dp)
            .roundedClickable(onClick = onClick)
            .padding(10.dp),
    )
}

@Composable
internal fun QuestionMarkIcon(
    tint: Color = Web3ModalTheme.colors.foreground.color100,
    onClick: () -> Unit
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_question_mark),
        contentDescription = ContentDescription.QUESTION_MARK.description,
        tint = tint,
        modifier = Modifier
            .size(36.dp)
            .roundedClickable(onClick = onClick)
            .padding(10.dp),
    )
}

@Composable
internal fun CloseIcon(
    tint: Color = Web3ModalTheme.colors.foreground.color100,
    onClick: () -> Unit
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_close),
        contentDescription = ContentDescription.CLOSE.description,
        tint = tint,
        modifier = Modifier
            .size(36.dp)
            .roundedClickable(onClick = onClick)
            .padding(10.dp),
    )
}

@Composable
@UiModePreview
private fun IconsPreview() {
    MultipleComponentsPreview(
        { BackArrowIcon {} },
        { QuestionMarkIcon {} },
        { CloseIcon {} }
    )
}