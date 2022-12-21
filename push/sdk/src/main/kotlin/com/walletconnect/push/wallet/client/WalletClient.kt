package com.walletconnect.push.wallet.client

object WalletClient: WalletInterface by WalletProtocol.instance {
    interface Delegate: WalletInterface.Delegate
}