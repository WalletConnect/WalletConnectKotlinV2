package com.walletconnect.push.wallet.client

object PushWalletClient: PushWalletInterface by PushWalletProtocol.instance {
    interface Delegate: PushWalletInterface.Delegate
}