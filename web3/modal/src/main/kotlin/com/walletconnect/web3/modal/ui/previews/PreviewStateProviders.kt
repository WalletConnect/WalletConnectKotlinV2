package com.walletconnect.web3.modal.ui.previews

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.walletconnect.web3.modal.ui.routes.connect.connect_wallet.ConnectYourWalletUI

internal class ConnectYourWalletStateProvider : PreviewParameterProvider<ConnectYourWalletUI> {
    override val values: Sequence<ConnectYourWalletUI>
        get() = sequenceOf(
            ConnectYourWalletUI(),
            ConnectYourWalletUI(walletsRecommendations)
        )
}
