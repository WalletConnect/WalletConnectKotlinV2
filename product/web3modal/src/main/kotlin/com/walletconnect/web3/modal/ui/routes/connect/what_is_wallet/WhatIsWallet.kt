package com.walletconnect.web3.modal.ui.routes.connect.what_is_wallet

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.components.internal.commons.HelpSection
import com.walletconnect.web3.modal.ui.components.internal.commons.WalletIcon
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonSize
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ButtonStyle
import com.walletconnect.web3.modal.ui.components.internal.commons.button.ImageButton
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.Web3ModalPreview

@Composable
internal fun WhatIsWallet(
    navController: NavController
) {
    WhatIsWallet(
        onGetWalletClick = { navController.navigate(Route.GET_A_WALLET.path) }
    )
}

@Composable
private fun WhatIsWallet(onGetWalletClick: () -> Unit) {
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
        Spacer(modifier = Modifier.height(4.dp))
        HelpSection(
            title = "A home for your digital assets",
            body = "A wallet lets you store, send and receive digital assets like cryptocurrencies and NFTs.",
            assets = listOf(R.drawable.defi, R.drawable.nft, R.drawable.eth)
        )
        Spacer(modifier = Modifier.height(4.dp))
        HelpSection(
            title = "Your gateway to a new web",
            body = "With your wallet, you can explore and interact with DeFi, NFTs, DAOs, and much more.",
            assets = listOf(R.drawable.browser, R.drawable.noun, R.drawable.dao)
        )
        Spacer(modifier = Modifier.height(10.dp))
        ImageButton(
            text = "Get a Wallet",
            image = { WalletIcon(it) },
            style = ButtonStyle.MAIN,
            size = ButtonSize.S,
            onClick = onGetWalletClick
        )
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