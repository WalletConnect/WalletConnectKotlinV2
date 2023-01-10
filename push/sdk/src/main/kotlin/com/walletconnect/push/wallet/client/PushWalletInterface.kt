package com.walletconnect.push.wallet.client

import com.walletconnect.push.common.Push

interface PushWalletInterface {
    interface Delegate {
        fun onPushRequest(pushRequest: Push.Wallet.Event.Request)

        fun onPushMessage(pushMessage: Push.Wallet.Event.Message)

        fun onPushDelete(pushDeletedTopic: Push.Wallet.Event.Delete)

        fun onError(error: Push.Model.Error)
    }

    fun initialize(init: Push.Wallet.Params.Init, onError: (Push.Model.Error) -> Unit)

    fun setDelegate(delegate: Delegate)

    fun approve(params: Push.Wallet.Params.Approve, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit)

    fun reject(params: Push.Wallet.Params.Reject, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit)

    fun getActiveSubscriptions(): Map<String, Push.Model.Subscription>

    fun delete(params: Push.Wallet.Params.Delete, onError: (Push.Model.Error) -> Unit)

    fun decryptMessage(params: Push.Wallet.Params.DecryptMessage, onSuccess: (String) -> Unit, onError: (Push.Model.Error) -> Unit)
}