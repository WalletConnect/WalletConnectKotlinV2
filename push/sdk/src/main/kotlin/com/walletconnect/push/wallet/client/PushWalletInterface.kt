package com.walletconnect.push.wallet.client

import com.walletconnect.push.common.Push

interface PushWalletInterface {
    interface Delegate {
        fun onPushRequest(pushRequest: Push.Wallet.Event.Request)

        fun onPushMessage(pushMessage: Push.Wallet.Event.Message)

        fun onPushDelete(pushDelete: Push.Wallet.Event.Delete)

        fun onPushSubscription(pushSubscribe: Push.Wallet.Event.Subscription)

        fun onError(error: Push.Model.Error)
    }

    fun initialize(init: Push.Wallet.Params.Init, onError: (Push.Model.Error) -> Unit)

    fun setDelegate(delegate: Delegate)

    fun approve(params: Push.Wallet.Params.Approve, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit)

    fun reject(params: Push.Wallet.Params.Reject, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit)

    fun subscribe(params: Push.Wallet.Params.Subscribe, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit)

    fun update(params: Push.Wallet.Params.Update, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getActiveSubscriptions(): Map<String, Push.Model.Subscription>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getMessageHistory(params: Push.Wallet.Params.MessageHistory): Map<Long, Push.Model.MessageRecord>

    fun deleteSubscription(params: Push.Wallet.Params.DeleteSubscription, onError: (Push.Model.Error) -> Unit)

    fun deletePushMessage(params: Push.Wallet.Params.DeleteMessage, onSuccess:() -> Unit, onError: (Push.Model.Error) -> Unit)

    fun decryptMessage(params: Push.Wallet.Params.DecryptMessage, onSuccess: (Push.Model.Message) -> Unit, onError: (Push.Model.Error) -> Unit)
}