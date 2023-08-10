package com.walletconnect.web3.modal.ui.components.internal

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import com.walletconnect.web3.modal.ui.components.internal.commons.BackArrowIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.CloseIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.QuestionMarkIcon
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview

@Composable
internal fun Web3ModalTopBar(
    title: String,
    startIcon: @Composable () -> Unit,
    onCloseIconClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        startIcon()
        Text(
            text = title,
            style = Web3ModalTheme.typo.paragraph700.copy(
                color = Web3ModalTheme.colors.foreground.color100,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.weight(1f)
        )
        CloseIcon(onClick = onCloseIconClick)
    }
}

@Composable
@UiModePreview
private fun PreviewWeb3ModalTopBar() {
    MultipleComponentsPreview(
        { Web3ModalTopBar(title = "WalletConnect", startIcon = { BackArrowIcon {} }, {}) },
        { Web3ModalTopBar(title = "WalletConnect", startIcon = { QuestionMarkIcon {} }, {}) }
    )
}
