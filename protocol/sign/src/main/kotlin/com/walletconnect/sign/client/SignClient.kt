package com.walletconnect.sign.client

@Deprecated("com.walletconnect.sign.client.SignClient has been deprecated. Please use com.reown.sign.client.SignClient instead from - https://github.com/reown-com/reown-kotlin")
object SignClient : SignInterface by SignProtocol.instance {
    @Deprecated("com.walletconnect.sign.client.WalletDelegate has been deprecated. Please use com.reown.sign.client.WalletDelegate instead from - https://github.com/reown-com/reown-kotlin")
    interface WalletDelegate: SignInterface.WalletDelegate
    @Deprecated("com.walletconnect.sign.client.DappDelegate has been deprecated. Please use com.reown.sign.client.DappDelegate instead from - https://github.com/reown-com/reown-kotlin")
    interface DappDelegate: SignInterface.DappDelegate
}