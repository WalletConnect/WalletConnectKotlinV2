package com.walletconnect.push.wallet.client

import com.walletconnect.push.common.Push

interface WalletInterface {

    fun initialize()

    fun approve(params: Push.Wallet.Params.Approve, onSuccess: (Boolean) -> Unit)

    fun reject(params: Push.Wallet.Params.Reject, onSuccess: (Boolean) -> Unit)

    fun getActiveSubscriptions(): Map<String, Push.Wallet.Model.Subscription>

    fun delete(params: Push.Wallet.Params.Delete)

    fun decryptMessage(params: Push.Wallet.Params.DecryptMessage, onSuccess: (Push.Wallet.Model.Message) -> Unit)
}