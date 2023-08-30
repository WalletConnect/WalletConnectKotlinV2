package com.walletconnect.web3.modal.ui.routes.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.modal.ui.components.common.roundedClickable
import com.walletconnect.web3.modal.ui.components.internal.commons.network.CircleNetworkImage
import com.walletconnect.web3.modal.ui.components.internal.commons.CloseIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.CompassIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.CopyIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.DisconnectIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.ExternalIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonSize
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonStyle
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ChipButton
import com.walletconnect.web3.modal.ui.components.internal.commons.entry.AccountEntry
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun AccountRoute(
    navController: NavController,
    closeModal: () -> Unit
) {
    AccountScreen(
        onCloseClick = { closeModal() },
        onBlockExplorerClick = {},
        onChangeNetworkClick = { navController.navigate(Route.CHANGE_NETWORK.path) },
        onDisconnectClick = {
            closeModal()
            // call disconnect
        }
    )
}

@Composable
private fun AccountScreen(
    onCloseClick: () -> Unit,
    onBlockExplorerClick: () -> Unit,
    onChangeNetworkClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        CloseIcon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(25.dp),
            onClick = onCloseClick
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 40.dp, bottom = 16.dp, start = 12.dp, end = 12.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AccountImage()
            VerticalSpacer(height = 20.dp)
            AccountAddress()
            Text(
                text = "0.527 ETH",
                style = Web3ModalTheme.typo.paragraph500.copy(Web3ModalTheme.colors.foreground.color200)
            )
            VerticalSpacer(height = 12.dp)
            ChipButton(
                text = "Block Explorer",
                startIcon = { CompassIcon() },
                endIcon = { ExternalIcon(it) },
                style = ButtonStyle.SHADE,
                size = ButtonSize.S,
                onClick = onBlockExplorerClick
            )
            VerticalSpacer(height = 20.dp)
            AccountEntry(
                startIcon = {
                    // Missing Account data model
                    CircleNetworkImage("")
                },
                onClick = onChangeNetworkClick
            ) {
                Text(text = "Ethereum", style = Web3ModalTheme.typo.paragraph600.copy(color = it.textColor))
            }
            VerticalSpacer(height = 8.dp)
            AccountEntry(
                startIcon = { DisconnectIcon() },
                onClick = onDisconnectClick
            ) {
                Text(text = "Disconnect", style = Web3ModalTheme.typo.paragraph600.copy(color = it.textColor))
            }
        }
    }
}

@Composable
private fun AccountImage() {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(Web3ModalTheme.colors.overlay10, shape = CircleShape)
    )
}

@Composable
private fun AccountAddress() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val clipboardManager: ClipboardManager = LocalClipboardManager.current
        // Missing account data
        val address = "0xa9...5608"
        Text(text = address, style = Web3ModalTheme.typo.title700)
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
private fun PreviewAccountScreen() {
    Web3ModalPreview {
        AccountScreen({}, {}, {}, {})
    }
}
