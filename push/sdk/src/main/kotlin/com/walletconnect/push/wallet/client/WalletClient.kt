package com.walletconnect.push.wallet.client

object WalletClient: WalletInterface by WalletProtocol.instance {
    interface WalletDelegate: WalletInterface.Delegate
}