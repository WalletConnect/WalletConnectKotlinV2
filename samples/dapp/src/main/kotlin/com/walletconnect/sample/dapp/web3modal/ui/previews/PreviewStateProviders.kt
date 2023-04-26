package com.walletconnect.sample.dapp.web3modal.ui.previews

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.walletconnect.sample.dapp.web3modal.ui.routes.connect_wallet.ConnectYourWalletUI

internal class ConnectYourWalletStateProvider : PreviewParameterProvider<ConnectYourWalletUI> {
    override val values: Sequence<ConnectYourWalletUI>
        get() = sequenceOf(
            ConnectYourWalletUI(),
            ConnectYourWalletUI(walletsRecommendations)
        )
}