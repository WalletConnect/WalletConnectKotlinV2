package com.walletconnect.push.dapp.client

import com.walletconnect.push.common.Push

interface PushDappInterface {

    interface Delegate {

        fun onPushResponse(pushResponse: Push.Dapp.Event.Response)

        fun onPushRejected(rejection: Push.Dapp.Event.Rejected)

        fun onDelete(pushDelete: Push.Dapp.Event.Delete)

        fun onError(error: Push.Model.Error)
    }

    fun setDelegate(delegate: Delegate)

    fun initialize(init: Push.Dapp.Params.Init, onError: (Push.Model.Error) -> Unit)

    fun request(params: Push.Dapp.Params.Request, onSuccess: (Push.Dapp.Model.RequestId) -> Unit, onError: (Push.Model.Error) -> Unit)

    fun notify(params: Push.Dapp.Params.Notify, onError: (Push.Model.Error) -> Unit)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getActiveSubscriptions(): Map<String, Push.Model.Subscription>

    fun deleteSubscription(params: Push.Dapp.Params.Delete, onError: (Push.Model.Error) -> Unit)
}