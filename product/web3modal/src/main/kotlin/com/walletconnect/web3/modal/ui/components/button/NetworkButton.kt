package com.walletconnect.web3.modal.ui.components.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.domain.model.Chain
import com.walletconnect.web3.modal.ui.components.internal.commons.SelectNetworkIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonSize
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonStyle
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ImageButton
import com.walletconnect.web3.modal.ui.components.internal.commons.network.CircleNetworkImage
import com.walletconnect.web3.modal.ui.theme.ProvideWeb3ModalThemeComposition

//TODO finish with adding chain switching
@Composable
internal fun NetworkButton(
    web3ButtonState: Web3ButtonState,
) {
    val selectedChain = web3ButtonState.getSelectedChain()
    val image: @Composable () -> Unit = selectedChain?.let { chain: Chain ->
        { CircleNetworkImage(url = chain.imageUrl, size = 24.dp) }
    } ?: {
        SelectNetworkIcon()
    }
    NetworkButton(
        text = selectedChain?.name ?: "Select Network",
        image = image,
        isEnabled = true,
        onClick = {}
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
