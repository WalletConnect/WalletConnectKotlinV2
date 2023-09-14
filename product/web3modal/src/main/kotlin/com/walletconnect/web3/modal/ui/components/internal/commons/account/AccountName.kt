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
import com.walletconnect.web3.modal.domain.model.Chain
import com.walletconnect.web3.modal.domain.model.Identity
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun AccountName(accountData: AccountData) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val clipboardManager: ClipboardManager = LocalClipboardManager.current
        val name = accountData.identity.name ?: "${accountData.address.take(4)}...${accountData.address.takeLast(4)}"
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
    val accountData = AccountData(
        topic = "",
        address = "0xd2B8b483056b134f9D8cd41F55bB065F9",
        balance = "543 ETH",
        selectedChain = Chain("eip155:1"),
        chains = listOf(Chain("eip155:1")),
        identity = Identity()
    )

    ComponentPreview {
        AccountName(accountData)
    }
}

@UiModePreview
@Composable
private fun AccountNamePreview() {
    val accountData = AccountData(
        topic = "",
        address = "0xd2B8b483056b134f9D8cd41F55bB065F9",
        balance = "543 ETH",
        selectedChain = Chain("eip155:1"),
        chains = listOf(Chain("eip155:1")),
        identity = Identity(name = "test.eth")
    )

    ComponentPreview {
        AccountName(accountData)
    }
}
