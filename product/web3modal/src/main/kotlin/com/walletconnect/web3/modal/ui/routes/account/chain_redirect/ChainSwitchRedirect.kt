package com.walletconnect.web3.modal.ui.routes.account.chain_redirect

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.ui.components.internal.commons.DeclinedIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.LoadingHexagonBorder
import com.walletconnect.web3.modal.ui.components.internal.commons.VerticalSpacer
import com.walletconnect.web3.modal.ui.components.internal.commons.button.TryAgainButton
import com.walletconnect.web3.modal.ui.components.internal.commons.network.HexagonNetworkImage
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.previews.testChains
import com.walletconnect.web3.modal.ui.routes.account.AccountState
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import com.walletconnect.web3.modal.utils.getImageData

@Composable
internal fun ChainSwitchRedirectRoute(
    accountState: AccountState,
    chain: Modal.Model.Chain,
) {
    var chainSwitchState by remember { mutableStateOf<ChainRedirectState>(ChainRedirectState.Loading) }

    ChainSwitchRedirectScreen(chain = chain, chainRedirectState = chainSwitchState)
}

@Composable
private fun ChainSwitchRedirectScreen(
    chain: Modal.Model.Chain,
    chainRedirectState: ChainRedirectState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, bottom = 40.dp, start = 40.dp, end = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ChainNetworkImage(
            chain = chain,
            redirectState = chainRedirectState
        )
        VerticalSpacer(height = 12.dp)
        ChainSwitchInfo(redirectState = chainRedirectState)
        VerticalSpacer(height = 20.dp)
        AnimatedVisibility(visible = chainRedirectState == ChainRedirectState.Declined) {
            TryAgainButton {}
        }

    }
}

@Composable
private fun ChainSwitchInfo(redirectState: ChainRedirectState) {
    AnimatedContent(targetState = redirectState, label = "Chain switch info") { state ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = state.toTitle(), style = Web3ModalTheme.typo.paragraph500)
            VerticalSpacer(height = 8.dp)
            Text(
                text = state.toInformation(),
                style = Web3ModalTheme.typo.small400.copy(Web3ModalTheme.colors.foreground.color200, textAlign = TextAlign.Center)
            )
        }
    }
}

private fun ChainRedirectState.toTitle() = when (this) {
    ChainRedirectState.Declined -> "Switch declined"
    ChainRedirectState.Loading -> "Approve in wallet"
}

private fun ChainRedirectState.toInformation() = when (this) {
    ChainRedirectState.Declined -> "Switch can be declined if chain is not supported by a wallet or previous request is still active"
    ChainRedirectState.Loading -> "Accept connection request in your wallet"
}

@Composable
private fun ChainNetworkImage(
    chain: Modal.Model.Chain,
    redirectState: ChainRedirectState
) {
    ChainNetworkImageWrapper(redirectState) {
        HexagonNetworkImage(
            data = chain.getImageData(),
            isEnabled = true
        )
    }
}

@Composable
private fun ChainNetworkImageWrapper(
    redirectState: ChainRedirectState,
    content: @Composable () -> Unit
) {
    AnimatedContent(
        targetState = redirectState,
        label = "ChainNetworkImageWrapper"
    ) { state ->
        when(state) {
            ChainRedirectState.Declined -> {
                Box {
                    content()
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(Web3ModalTheme.colors.background.color100, shape = CircleShape)
                            .padding(2.dp)
                    ) {
                        DeclinedIcon()
                    }
                }
            }
            ChainRedirectState.Loading -> {
                LoadingHexagonBorder {
                    content()
                }
            }
        }
    }
}

@Composable
@UiModePreview
private fun ChainSwitchRedirectScreenWithLoadingStatePreview() {
    val chain = testChains.first()
    Web3ModalPreview(title = chain.chainName) {
        ChainSwitchRedirectScreen(chain, ChainRedirectState.Loading)
    }
}

@Composable
@UiModePreview
private fun ChainSwitchRedirectScreenWithDeclinedStatePreview() {
    val chain = testChains.first()
    Web3ModalPreview(title = chain.chainName) {
        ChainSwitchRedirectScreen(chain, ChainRedirectState.Declined)
    }
}

