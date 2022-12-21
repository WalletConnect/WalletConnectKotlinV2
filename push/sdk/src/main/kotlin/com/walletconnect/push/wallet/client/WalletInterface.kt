package com.walletconnect.push.wallet.client

import com.walletconnect.push.common.Push

interface WalletInterface {
    interface Delegate {
        fun onPushRequest(pushRequest: Push.Wallet.Event.Request)

        fun onPushMessage(message: Push.Wallet.Event.Message)

        fun onPushDelete(deletedTopic: Push.Wallet.Event.Delete)
    }

    fun initialize(init: Push.Wallet.Params.Init, onError: (Push.Model.Error) -> Unit)

    fun setDelegate(delegate: Delegate)

    fun approve(params: Push.Wallet.Params.Approve, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit)

    fun reject(params: Push.Wallet.Params.Reject, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit)

    fun getActiveSubscriptions(): Map<String, Push.Model.Subscription>

    fun delete(params: Push.Wallet.Params.Delete, onError: (Push.Model.Error) -> Unit)

    fun decryptMessage(params: Push.Wallet.Params.DecryptMessage, onSuccess: (Push.Model.Message) -> Unit, onError: (Push.Model.Error) -> Unit)
}