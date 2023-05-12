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
import com.walletconnect.web3.modal.ui.theme.provideWeb3ModalColors

@Composable
internal fun Web3ModalPreview(
    content: @Composable () -> Unit,
) {
    Web3ModalRoot(
        navController = rememberNavController(),
        colors = provideWeb3ModalColors(),
        closeModal = {}
    ) {
        content()
    }
}

@Composable
internal fun ComponentPreview(
    content: @Composable ColumnScope.() -> Unit
) {
    ProvideWeb3ModalThemeComposition(
        colors = provideWeb3ModalColors()
    ) {
        Column(modifier = Modifier.background(Web3ModalTheme.colors.background)) {
            content()
        }
    }
}
