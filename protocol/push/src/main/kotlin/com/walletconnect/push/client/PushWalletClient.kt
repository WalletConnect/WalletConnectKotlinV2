package com.walletconnect.push.client

object PushWalletClient: PushWalletInterface by PushWalletProtocol.instance {
    interface Delegate: PushWalletInterface.Delegate
}