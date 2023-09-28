package com.walletconnect.web3.modal.ui.routes.account.account

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.ui.components.internal.commons.CloseIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.CompassIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.DisconnectIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.ExternalIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.account.AccountName
import com.walletconnect.web3.modal.ui.components.internal.commons.account.AccountImage
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonSize
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonStyle
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ChipButton
import com.walletconnect.web3.modal.ui.components.internal.commons.entry.AccountEntry
import com.walletconnect.web3.modal.ui.components.internal.commons.entry.AccountEntryState
import com.walletconnect.web3.modal.ui.components.internal.commons.network.CircleNetworkImage
import com.walletconnect.web3.modal.ui.model.UiStateBuilder
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.previews.ethereumChain
import com.walletconnect.web3.modal.ui.routes.account.AccountState
import com.walletconnect.web3.modal.ui.previews.accountDataPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import com.walletconnect.web3.modal.utils.getImageData

@Composable
internal fun AccountRoute(
    navController: NavController,
    accountState: AccountState,
    closeModal: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val selectedChain by accountState.selectedChain.collectAsState(initial = Web3Modal.getSelectedChainOrFirst())

    Box(modifier = Modifier.fillMaxWidth()) {
        CloseIcon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(25.dp),
            onClick = { closeModal() }
        )
        UiStateBuilder(uiStateFlow = accountState.accountState) { data ->
            AccountScreen(
                accountData = data,
                selectedChain = selectedChain,
                onBlockExplorerClick = { url -> uriHandler.openUri(url) },
                onChangeNetworkClick = { navController.navigate(Route.CHANGE_NETWORK.path) },
                onDisconnectClick = { accountState.disconnect(data.topic) { closeModal() } }
            )
        }
    }
}

@Composable
private fun AccountScreen(
    accountData: AccountData,
    selectedChain: Modal.Model.Chain,
    onBlockExplorerClick: (String) -> Unit,
    onChangeNetworkClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 40.dp, bottom = 16.dp, start = 12.dp, end = 12.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AccountImage(address = accountData.address, avatarUrl = accountData.identity?.avatar)
        VerticalSpacer(height = 20.dp)
        AccountName(accountData)
        accountData.balance?.let { balance ->
            VerticalSpacer(height = 8.dp)
            Text(
                text = balance,
                style = Web3ModalTheme.typo.paragraph500.copy(Web3ModalTheme.colors.foreground.color200)
            )
        }
        selectedChain.blockExplorerUrl?.let { url ->
            VerticalSpacer(height = 12.dp)
            ChipButton(
                text = "Block Explorer",
                startIcon = { CompassIcon() },
                endIcon = { ExternalIcon(it) },
                style = ButtonStyle.SHADE,
                size = ButtonSize.S,
                onClick = { onBlockExplorerClick("$url/address/${accountData.address}") }
            )
        }
        VerticalSpacer(height = 20.dp)
        AccountEntry(
            startIcon = { CircleNetworkImage(selectedChain.getImageData()) },
            onClick = onChangeNetworkClick,
            state = if (accountData.chains.size == 1) AccountEntryState.INFO else AccountEntryState.NEXT
        ) {
            Text(text = selectedChain.chainName, style = Web3ModalTheme.typo.paragraph600.copy(color = it.textColor))
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

@UiModePreview
@Composable
private fun PreviewAccountScreen() {
    Web3ModalPreview {
        AccountScreen(accountDataPreview, ethereumChain, {}, {}, {})
    }
}
