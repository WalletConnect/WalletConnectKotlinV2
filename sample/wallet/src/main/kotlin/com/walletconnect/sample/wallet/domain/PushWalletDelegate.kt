package com.walletconnect.sample.wallet.domain

import com.walletconnect.push.client.Push
import com.walletconnect.push.client.PushWalletClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

object PushWalletDelegate : PushWalletClient.Delegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcPushEventModels: MutableSharedFlow<Push.Event?> = MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val wcPushEventModels: SharedFlow<Push.Event?> = _wcPushEventModels

    init {
        PushWalletClient.setDelegate(this)
    }

    override fun onPushProposal(pushProposal: Push.Event.Proposal) {
        scope.launch { _wcPushEventModels.emit(pushProposal) }
    }

    override fun onPushMessage(pushMessage: Push.Event.Message) {
        scope.launch { _wcPushEventModels.emit(pushMessage) }
    }

    override fun onPushDelete(pushDelete: Push.Event.Delete) {
        scope.launch { _wcPushEventModels.emit(pushDelete) }
    }

    override fun onPushSubscription(pushSubscribe: Push.Event.Subscription) {
        scope.launch { _wcPushEventModels.emit(pushSubscribe) }
    }

    override fun onPushUpdate(pushUpdate: Push.Event.Update) {
        scope.launch { _wcPushEventModels.emit(pushUpdate) }
    }

    override fun onError(error: Push.Model.Error) {
    }
}