package com.walletconnect.web3.modal.ui.components.internal.commons.inputs

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.ContentDescription
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun InputCancel(
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val background: Color
    val tint: Color

    if (isEnabled) {
        tint = Web3ModalTheme.colors.background.color100
        background = Web3ModalTheme.colors.overlay20
    } else {
        tint = Web3ModalTheme.colors.background.color100
        background = Web3ModalTheme.colors.overlay10
    }

    Surface(
        color = background,
        modifier = Modifier.size(18.dp).clickable { onClick() }.then(modifier),
        shape = RoundedCornerShape(6.dp)
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_close),
            contentDescription = ContentDescription.CLEAR.description,
            modifier = Modifier.size(10.dp).padding(3.dp),
            colorFilter = ColorFilter.tint(tint)
        )
    }
}

@UiModePreview
@Composable
private fun PreviewInputCancel() {
    MultipleComponentsPreview(
        { InputCancel() {} },
        { InputCancel(isEnabled = false) {} },
    )
}
