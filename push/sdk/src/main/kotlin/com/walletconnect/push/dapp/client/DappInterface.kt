package com.walletconnect.push.dapp.client

import com.walletconnect.push.common.Push

interface DappInterface {

    interface DappDelegate {

        fun onPushResponse(pushResponse: Push.Event.Response)
    }

    fun initialize(init: Push.Params.Init, onError: (Push.Model.Error) -> Unit)

    fun request(params: Push.Params.Request, onSuccess: (Push.Model.RequestId) -> Unit, onError: (Push.Model.Error) -> Unit)

    fun notify(params: Push.Params.Notify, onError: (Push.Model.Error) -> Unit)

    fun getActiveSubscriptions(): Map<String, Push.Model.Subscription>

    fun delete(params: Push.Params.Delete, onError: (Push.Model.Error) -> Unit)
}