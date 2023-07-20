package com.walletconnect.web3.modal.ui.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalRoot
import com.walletconnect.web3.modal.ui.theme.ProvideWeb3ModalThemeComposition
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun Web3ModalPreview(
    content: @Composable () -> Unit,
) {
    Web3ModalRoot{
        content()
    }
}

@Composable
internal fun ComponentPreview(
    content: @Composable ColumnScope.() -> Unit
) {
    ProvideWeb3ModalThemeComposition {
        Column(modifier = Modifier.background(Web3ModalTheme.colors.background.color100)) {
            content()
        }
    }
}
