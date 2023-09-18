package com.walletconnect.web3.modal.ui.components.internal.commons.account

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.walletconnect.modal.ui.components.common.roundedClickable
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.domain.model.Identity
import com.walletconnect.web3.modal.ui.components.internal.commons.CopyIcon
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.accountDataPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import com.walletconnect.web3.modal.utils.toVisibleAddress

@Composable
internal fun AccountName(accountData: AccountData) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val clipboardManager: ClipboardManager = LocalClipboardManager.current
        val name = accountData.identity.name ?: accountData.address.toVisibleAddress()
        Text(text = name, style = Web3ModalTheme.typo.title700)
        CopyIcon(
            modifier = Modifier
                .size(32.dp)
                .padding(8.dp)
                .roundedClickable { clipboardManager.setText(AnnotatedString(accountData.address)) }
        )
    }
}

@UiModePreview
@Composable
private fun AccountAddressPreview() {
    ComponentPreview {
        AccountName(accountDataPreview)
    }
}

@UiModePreview
@Composable
private fun AccountNamePreview() {
    ComponentPreview {
        AccountName(accountDataPreview.copy(identity = Identity(name = "testIdentity.eth")))
    }
}
