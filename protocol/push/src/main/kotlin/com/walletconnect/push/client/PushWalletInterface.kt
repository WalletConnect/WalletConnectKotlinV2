package com.walletconnect.push.client

interface PushWalletInterface {
    interface Delegate {

        @Deprecated("The onPushProposal method is no longer supported and will be removed in a future release")
        fun onPushProposal(pushProposal: Push.Event.Proposal)

        fun onPushSubscription(pushSubscribe: Push.Event.Subscription)

        fun onPushMessage(pushMessage: Push.Event.Message)

        fun onPushDelete(pushDelete: Push.Event.Delete)

        fun onPushUpdate(pushUpdate: Push.Event.Update)

        fun onError(error: Push.Model.Error)
    }

    fun initialize(init: Push.Params.Init, onError: (Push.Model.Error) -> Unit)

    fun setDelegate(delegate: Delegate)

    fun approve(params: Push.Params.Approve, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit)

    fun reject(params: Push.Params.Reject, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit)

    fun subscribe(params: Push.Params.Subscribe, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit)

    fun update(params: Push.Params.Update, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getActiveSubscriptions(): Map<String, Push.Model.Subscription>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getMessageHistory(params: Push.Params.MessageHistory): Map<Long, Push.Model.MessageRecord>

    fun deleteSubscription(params: Push.Params.DeleteSubscription, onError: (Push.Model.Error) -> Unit)

    fun deletePushMessage(params: Push.Params.DeleteMessage, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit)

    fun decryptMessage(params: Push.Params.DecryptMessage, onSuccess: (Push.Model.Message) -> Unit, onError: (Push.Model.Error) -> Unit)

    fun enableSync(params: Push.Params.EnableSync, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit)
}