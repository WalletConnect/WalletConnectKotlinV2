package com.walletconnect.sign.client

object SignClient : SignInterface by SignProtocol.instance {
    object WebSocket : SignInterface.Websocket by SignProtocol.instance
    interface WalletDelegate: SignInterface.WalletDelegate
    interface DappDelegate: SignInterface.DappDelegate
}