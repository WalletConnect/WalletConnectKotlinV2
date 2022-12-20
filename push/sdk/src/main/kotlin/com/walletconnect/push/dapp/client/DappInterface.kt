package com.walletconnect.push.dapp.client

import com.walletconnect.push.common.Push

interface DappInterface {

    interface Delegate {

        fun onPushResponse(pushResponse: Push.Dapp.Event.Response)
    }

    fun setDelegate(delegate: DappInterface.Delegate)

    fun initialize(init: Push.Dapp.Params.Init, onError: (Push.Dapp.Model.Error) -> Unit)

    fun request(params: Push.Dapp.Params.Request, onSuccess: (Push.Dapp.Model.RequestId) -> Unit, onError: (Push.Dapp.Model.Error) -> Unit)

    fun notify(params: Push.Dapp.Params.Notify, onError: (Push.Dapp.Model.Error) -> Unit)

    fun getActiveSubscriptions(): Map<String, Push.Dapp.Model.Subscription>

    fun delete(params: Push.Dapp.Params.Delete, onError: (Push.Dapp.Model.Error) -> Unit)
}