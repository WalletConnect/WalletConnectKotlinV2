package com.walletconnect.dapp.domain

import com.walletconnect.push.common.Push
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.dapp.client.DappClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

object PushDappDelegate: DappClient.Delegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcPushEventModels: MutableSharedFlow<Push.Dapp.Event?> = MutableSharedFlow(1)
    val wcPushEventModels: SharedFlow<Push.Dapp.Event?> = _wcPushEventModels

    lateinit var activePushSubscription: Push.Model.Subscription

    init {
        DappClient.setDelegate(this)
    }

    override fun onPushResponse(pushResponse: Push.Dapp.Event.Response) {
        activePushSubscription = pushResponse.subscription

        scope.launch { _wcPushEventModels.emit(pushResponse) }
    }

    override fun onPushRejected(rejection: Push.Dapp.Event.Rejected) {
    }

    override fun onDelete(pushDelete: Push.Dapp.Event.Delete) {

    }

    override fun onError(error: Push.Model.Error) {
    }
}