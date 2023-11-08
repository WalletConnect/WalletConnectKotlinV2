package com.walletconnect.web3.modal.ui.components.internal.commons.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

internal enum class AccountEntryState {
    LOADING, NEXT, INFO
}

@Composable
internal fun AccountEntry(
    isEnabled: Boolean = true,
    state: AccountEntryState = AccountEntryState.NEXT,
    onClick: () -> Unit,
    startIcon: @Composable (Boolean) -> Unit,
    content: @Composable (EntryColors) -> Unit,
) {
    BaseEntry(isEnabled = isEnabled) { entryColors ->
        Row(
            modifier = Modifier
                .clickable(enabled = state == AccountEntryState.NEXT && isEnabled) { onClick() }
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
            when(state) {
                AccountEntryState.LOADING -> LoadingSpinner(tint = entryColors.secondaryColor)
                AccountEntryState.NEXT -> ChevronRightIcon(entryColors.secondaryColor)
                AccountEntryState.INFO -> {}
            }
            HorizontalSpacer(width = 6.dp)
        }
    }
}

@UiModePreview
@Composable
private fun AccountEntryPreview() {
    val content: @Composable (EntryColors) -> Unit = { Text(text = "Account entry", style = Web3ModalTheme.typo.paragraph500.copy(color = it.textColor))}
    MultipleComponentsPreview(
        {
            AccountEntry(
                onClick = {},
                startIcon = { CircleNetworkImage(data = "", isEnabled = it, placeholder = ContextCompat.getDrawable(LocalContext.current, R.drawable.defi)) }, content = content)
        },
        {
            AccountEntry(
                onClick = {},
                startIcon = { CircleNetworkImage(data = "", isEnabled = it, placeholder = ContextCompat.getDrawable(LocalContext.current, R.drawable.defi)) },
                state = AccountEntryState.LOADING,
                content = content
            )
        },
        {
            AccountEntry(
                onClick = {},
                startIcon = { CircleNetworkImage(data = "", isEnabled = it, placeholder = ContextCompat.getDrawable(LocalContext.current, R.drawable.defi)) },
                state = AccountEntryState.INFO,
                content = content
            )
        },
        {
            AccountEntry(
                onClick = {},
                startIcon = { CircleNetworkImage(data = "", isEnabled = it, placeholder = ContextCompat.getDrawable(LocalContext.current, R.drawable.defi)) },
                isEnabled = false,
                content = content
            )
        }
    )
}
