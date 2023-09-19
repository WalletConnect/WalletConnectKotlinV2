package com.walletconnect.web3.modal.ui.components.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.model.Chain
import com.walletconnect.web3.modal.ui.components.internal.commons.SelectNetworkIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonSize
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonStyle
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ImageButton
import com.walletconnect.web3.modal.ui.components.internal.commons.network.CircleNetworkImage
import com.walletconnect.web3.modal.ui.theme.ProvideWeb3ModalThemeComposition
import com.walletconnect.web3.modal.utils.getImageData

@Composable
fun NetworkButton(
    state: Web3ModalState,
) {
    val selectedChain by state.selectedChain.collectAsState(initial = null)
    val image: @Composable () -> Unit = selectedChain?.let { chain ->
        { CircleNetworkImage(data = chain.getImageData(), size = 24.dp) }
    } ?: {
        SelectNetworkIcon()
    }
    NetworkButton(
        text = selectedChain?.chainName ?: "Select Network",
        image = image,
        isEnabled = true,
        onClick = { state.openWeb3Modal(true) }
    )
}

@Composable
internal fun NetworkButton(
    text: String,
    image: @Composable () -> Unit,
    onClick: () -> Unit,
    isEnabled: Boolean = true
) {
    ProvideWeb3ModalThemeComposition {
        ImageButton(
            text = text,
            image = { image() },
            isEnabled = isEnabled,
            style = ButtonStyle.ACCOUNT,
            size = ButtonSize.ACCOUNT_M,
            onClick = onClick
        )
    }
}
