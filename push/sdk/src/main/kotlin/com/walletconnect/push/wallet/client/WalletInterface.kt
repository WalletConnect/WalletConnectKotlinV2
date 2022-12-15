package com.walletconnect.push.wallet.client

import com.walletconnect.push.common.Push

interface WalletInterface {
    interface Delegate {
        fun onPushRequest(pushRequest: Push.Wallet.Model.Request)

        fun onPushMessage(message: Push.Wallet.Model.Message)
    }

    fun initialize(onError: (Push.Wallet.Model.Error) -> Unit)

    fun setDelegate(delegate: Delegate)

    fun approve(params: Push.Wallet.Params.Approve, onSuccess: (Boolean) -> Unit, onError: (Push.Wallet.Model.Error) -> Unit)

    fun reject(params: Push.Wallet.Params.Reject, onSuccess: (Boolean) -> Unit, onError: (Push.Wallet.Model.Error) -> Unit)

    fun getActiveSubscriptions(): Map<String, Push.Wallet.Model.Subscription>

    fun delete(params: Push.Wallet.Params.Delete)

    fun decryptMessage(params: Push.Wallet.Params.DecryptMessage, onSuccess: (Push.Wallet.Model.Message) -> Unit, onError: (Push.Wallet.Model.Error) -> Unit)
}