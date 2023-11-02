package com.walletconnect.web3.modal.ui.routes.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.ExternalIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.HelpSection
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonSize
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonStyle
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ImageButton
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun WhatIsNetworkRoute() {
    val uriHandler = LocalUriHandler.current

    WhatIsNetwork {
        uriHandler.openUri("https://ethereum.org/en/developers/docs/networks/")
    }
}

@Composable
private fun WhatIsNetwork(
    onLearnMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        HelpSection(
            title = "The system’s nuts and bolts",
            body = "A network is what brings the blockchain to life, as this technical infrastructure allows apps to access the ledger and smart contract services.",
            assets = listOf(R.drawable.network, R.drawable.layers, R.drawable.system)
        )
        Spacer(modifier = Modifier.height(4.dp))
        HelpSection(
            title = "Designed for different uses",
            body = "Each network is designed differently, and may therefore suit certain apps and experiences.",
            assets = listOf(R.drawable.noun, R.drawable.defi_alt, R.drawable.dao)
        )
        Spacer(modifier = Modifier.height(10.dp))
        ImageButton(
            text = "Learn more",
            image = { ExternalIcon(Web3ModalTheme.colors.accent100) },
            style = ButtonStyle.MAIN,
            size = ButtonSize.S,
            onClick = onLearnMoreClick
        )
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@UiModePreview
@Composable
private fun WhatIsNetworkPreview() {
    Web3ModalPreview(title = "What is a network?") {
        WhatIsNetworkRoute()
    }
}
