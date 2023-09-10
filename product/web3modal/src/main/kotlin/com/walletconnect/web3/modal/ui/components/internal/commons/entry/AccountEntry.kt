package com.walletconnect.web3.modal.ui.components.internal.commons.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.ChevronRightIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.HorizontalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.LoadingSpinner
import com.walletconnect.web3.modal.ui.components.internal.commons.network.CircleNetworkImage
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun AccountEntry(
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    startIcon: @Composable (Boolean) -> Unit,
    content: @Composable (EntryColors) -> Unit,
) {
    BaseEntry(isEnabled = isEnabled) { entryColors ->
        Row(
            modifier = Modifier
                .clickable(enabled = isEnabled) { onClick() }
                .height(56.dp)
                .background(color = entryColors.background)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            startIcon(isEnabled)
            HorizontalSpacer(width = 12.dp)
            Box(modifier = Modifier.weight(1f)) {
                content(entryColors)
            }
            HorizontalSpacer(width = 12.dp)
            if (isLoading) {
                LoadingSpinner(entryColors.secondaryColor)
            } else {
                ChevronRightIcon(entryColors.secondaryColor)
            }
        }
    }
}

@UiModePreview
@Composable
private fun AccountEntryPreview() {
    val content: @Composable (EntryColors) -> Unit = { Text(text = "Account entry", style = Web3ModalTheme.typo.paragraph600.copy(color = it.textColor))}
    MultipleComponentsPreview(
        {
            AccountEntry(
                onClick = {},
                startIcon = { CircleNetworkImage(url = "", isEnabled = it, placeholder = ContextCompat.getDrawable(LocalContext.current, R.drawable.defi)) }, content = content)
        },
        {
            AccountEntry(
                onClick = {},
                startIcon = { CircleNetworkImage(url = "", isEnabled = it, placeholder = ContextCompat.getDrawable(LocalContext.current, R.drawable.defi)) },
                isLoading = true,
                content = content
            )
        },
        {
            AccountEntry(
                onClick = {},
                startIcon = { CircleNetworkImage(url = "", isEnabled = it, placeholder = ContextCompat.getDrawable(LocalContext.current, R.drawable.defi)) },
                isEnabled = false,
                content = content
            )
        }
    )
}
