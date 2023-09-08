package com.walletconnect.web3.modal.ui.routes.account

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.domain.model.Chain
import com.walletconnect.web3.modal.ui.components.internal.commons.CloseIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.CompassIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.DisconnectIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.ExternalIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.account.AccountAddress
import com.walletconnect.web3.modal.ui.components.internal.commons.account.AccountImage
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonSize
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonStyle
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ChipButton
import com.walletconnect.web3.modal.ui.components.internal.commons.entry.AccountEntry
import com.walletconnect.web3.modal.ui.components.internal.commons.entry.AccountEntryState
import com.walletconnect.web3.modal.ui.components.internal.commons.network.CircleNetworkImage
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun AccountRoute(
    navController: NavController,
    accountData: AccountData,
    disconnect: (String) -> Unit,
    closeModal: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    AccountScreen(
        accountData = accountData,
        onCloseClick = { closeModal() },
        onBlockExplorerClick = { uriHandler.openBlockExplorer(accountData.address) },
        onChangeNetworkClick = { navController.navigate(Route.CHANGE_NETWORK.path) },
        onDisconnectClick = { disconnect(accountData.topic) }
    )
}

@Composable
private fun AccountScreen(
    accountData: AccountData,
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
            AccountImage(accountData.address)
            VerticalSpacer(height = 20.dp)
            AccountAddress(accountData.address)
            Text(
                text = accountData.balance,
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
                startIcon = { CircleNetworkImage(accountData.selectedChain.imageUrl) },
                onClick = onChangeNetworkClick,
                state = if(accountData.chains.size == 1) AccountEntryState.INFO else AccountEntryState.NEXT
            ) {
                Text(text = accountData.selectedChain.name, style = Web3ModalTheme.typo.paragraph600.copy(color = it.textColor))
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

private fun UriHandler.openBlockExplorer(address: String) {
    openUri("https://etherscan.io/address/$address")
}

@UiModePreview
@Composable
private fun PreviewAccountScreen() {
    Web3ModalPreview {
        val accountData = AccountData(
            topic = "",
            address = "0xd2B8b483056b134f9D8cd41F55bB065F9",
            balance = "543 ETH",
            selectedChain = Chain("eip155:1"),
            chains = listOf(Chain("eip155:1"))
        )
        AccountScreen(accountData, {}, {}, {}, {})
    }
}
