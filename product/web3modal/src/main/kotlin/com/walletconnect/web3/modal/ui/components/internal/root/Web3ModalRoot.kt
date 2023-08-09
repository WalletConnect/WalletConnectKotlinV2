package com.walletconnect.web3.modal.ui.components.internal.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalTopBar
import com.walletconnect.web3.modal.ui.components.internal.commons.QuestionMarkIcon
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.theme.ProvideWeb3ModalThemeComposition
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun Web3ModalRoot(
    navController: NavHostController,
    content: @Composable () -> Unit
) {


    Column(
        verticalArrangement = Arrangement.Bottom
    ) {
        ProvideWeb3ModalThemeComposition() {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Web3ModalTheme.colors.background.color100)
            ) {
                Web3ModalTopBar(
                    title = "Connect your wallet",
                    startIcon = { QuestionMarkIcon(onClick = { navController.navigate(Route.Help.path) })},
                    onCloseIconClick = {}
                )
                content()
            }
        }
    }
}

@Composable
@Preview
private fun PreviewWeb3ModalRoot() {
    ComponentPreview {
        Web3ModalRoot(rememberNavController()) {
            Box(modifier = Modifier.size(500.dp))
        }
    }
}
