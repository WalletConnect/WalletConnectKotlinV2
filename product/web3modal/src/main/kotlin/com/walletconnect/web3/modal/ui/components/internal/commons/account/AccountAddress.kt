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
import com.walletconnect.web3.modal.ui.components.internal.commons.CopyIcon
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import com.walletconnect.web3.modal.utils.toVisibleAddress

@Composable
internal fun AccountAddress(address: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val clipboardManager: ClipboardManager = LocalClipboardManager.current
        Text(text = address.toVisibleAddress(), style = Web3ModalTheme.typo.title700)
        CopyIcon(
            modifier = Modifier
                .size(32.dp)
                .padding(8.dp)
                .roundedClickable { clipboardManager.setText(AnnotatedString(address)) }
        )
    }
}

@UiModePreview
@Composable
private fun AccountAddressPreview() {
    ComponentPreview {
        AccountAddress("0x59eAF7DD5a2f5e433083D8BbC8de3439542579cb")
    }
}
