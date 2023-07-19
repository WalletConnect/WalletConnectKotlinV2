package com.walletconnect.web3.modal.ui.components.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.walletconnect.web3.modal.ui.components.internal.commons.CloseIconButton
import com.walletconnect.web3.modal.ui.components.internal.commons.QuestionMarkIconButton
import com.walletconnect.web3.modal.ui.components.internal.walletconnect.WalletConnectLogo
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.theme.ProvideWeb3ModalThemeComposition
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun Web3ModalRoot(
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Bottom
    ) {
        ProvideWeb3ModalThemeComposition() {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Web3ModalTheme.colors.background.color100,)
                ) {
                    content()
                }
        }
    }
}

@Composable
@Preview
private fun PreviewWeb3ModalRoot() {
    ComponentPreview {
        Web3ModalRoot {
            Box(modifier = Modifier.size(500.dp))
        }
    }
}
