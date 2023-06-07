package com.walletconnect.sample.wallet.domain

import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.PushWalletClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

object PushWalletDelegate: PushWalletClient.Delegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcPushEventModels: MutableSharedFlow<Push.Wallet.Event?> = MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val wcPushEventModels: SharedFlow<Push.Wallet.Event?> = _wcPushEventModels

    init {
        PushWalletClient.setDelegate(this)
    }

    @Deprecated("OnPushRequest will be removed in a future version. Use onPushProposal")
    override fun onPushRequest(pushRequest: Push.Wallet.Event.Request) {
        scope.launch { _wcPushEventModels.emit(pushRequest) }
    }

    override fun onPushProposal(pushProposal: Push.Wallet.Event.Proposal) {
        scope.launch { _wcPushEventModels.emit(pushProposal) }
    }

    override fun onPushMessage(pushMessage: Push.Wallet.Event.Message) {
        scope.launch { _wcPushEventModels.emit(pushMessage) }
    }

    override fun onPushDelete(pushDelete: Push.Wallet.Event.Delete) {
        scope.launch { _wcPushEventModels.emit(pushDelete) }
    }

    override fun onPushSubscription(pushSubscribe: Push.Wallet.Event.Subscription) {
        scope.launch { _wcPushEventModels.emit(pushSubscribe) }
    }

    override fun onPushUpdate(pushUpdate: Push.Wallet.Event.Update) {
        scope.launch { _wcPushEventModels.emit(pushUpdate) }
    }

    override fun onError(error: Push.Model.Error) {
    }
}