package com.walletconnect.web3.modal.ui.components.internal.commons.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.components.internal.commons.ChevronRightIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.HorizontalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.LoadingSpinner
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview

@Composable
internal fun AccountEntry(
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    startIcon: @Composable () -> Unit,
    content: @Composable (Color) -> Unit,
) {
    BaseEntry(isEnabled = isEnabled) { entryColors ->
        Row(
            modifier = Modifier
                .clickable(enabled = isEnabled) { onClick() }
                .height(56.dp)
                .background(color = entryColors.background)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            ) {
            startIcon()
            HorizontalSpacer(width = 12.dp)
            Box(modifier = Modifier.weight(1f)) {
                content(entryColors.textColor)
            }
            HorizontalSpacer(width = 12.dp)
            if (isLoading) {
                LoadingSpinner()
            } else {
                ChevronRightIcon()
            }
        }
    }
}

@UiModePreview
@Composable
private fun AccountEntryPreview() {
    MultipleComponentsPreview()
}
