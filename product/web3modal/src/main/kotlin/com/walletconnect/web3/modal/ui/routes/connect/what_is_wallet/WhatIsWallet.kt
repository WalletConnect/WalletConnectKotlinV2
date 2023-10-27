package com.walletconnect.web3.modal.ui.routes.connect.what_is_wallet

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.ExternalIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.HelpSection
import com.walletconnect.web3.modal.ui.components.internal.commons.WalletIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonSize
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonStyle
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ImageButton
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview

internal enum class WhatIsWalletOption {
    GET_WALLET, LEARN_MORE
}

@Composable
internal fun WhatIsWallet(
    navController: NavController,
    option: WhatIsWalletOption
) {
    val uriHandler = LocalUriHandler.current

    WhatIsWallet(
        optionButton = {
            ImageButton(
                text = option.label(),
                image = { option.Icon(it) },
                style = ButtonStyle.MAIN,
                size = ButtonSize.S,
                onClick = {
                    when (option) {
                        WhatIsWalletOption.GET_WALLET -> navController.navigate(Route.GET_A_WALLET.path)
                        WhatIsWalletOption.LEARN_MORE -> uriHandler.openUri("https://ethereum.org/en/developers/docs/networks/")
                    }
                }
            )
        }
    )
}

private fun WhatIsWalletOption.label() = when (this) {
    WhatIsWalletOption.GET_WALLET -> "Get a wallet"
    WhatIsWalletOption.LEARN_MORE -> "Learn more"
}

@Composable
private fun WhatIsWalletOption.Icon(tint: Color) {
    when (this) {
        WhatIsWalletOption.GET_WALLET -> WalletIcon(tint)
        WhatIsWalletOption.LEARN_MORE -> ExternalIcon(tint)
    }
}

@Composable
private fun WhatIsWallet(
    optionButton: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        HelpSection(
            title = "One login for all of web3",
            body = "Log in to any app by connecting your wallet. Say goodbye to countless passwords!",
            assets = listOf(R.drawable.login, R.drawable.profile, R.drawable.lock)
        )
        Spacer(modifier = Modifier.height(24.dp))
        HelpSection(
            title = "A home for your digital assets",
            body = "A wallet lets you store, send and receive digital assets like cryptocurrencies and NFTs.",
            assets = listOf(R.drawable.defi, R.drawable.nft, R.drawable.eth)
        )
        Spacer(modifier = Modifier.height(24.dp))
        HelpSection(
            title = "Your gateway to a new web",
            body = "With your wallet, you can explore and interact with DeFi, NFTs, DAOs, and much more.",
            assets = listOf(R.drawable.browser, R.drawable.noun, R.drawable.dao)
        )
        Spacer(modifier = Modifier.height(20.dp))
        optionButton()
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
@UiModePreview
private fun HelpContentPreview() {
    Web3ModalPreview("What is a Wallet?") {
        WhatIsWallet {}
    }
}