package com.walletconnect.wallet.domain

import com.walletconnect.android.Core
import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.WalletClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object PushDelegate: WalletClient.WalletDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcCoreEventModels: MutableSharedFlow<Core.Model?> = MutableSharedFlow(1)
    val wcCoreEventModels: SharedFlow<Core.Model?> = _wcCoreEventModels

    init {
        WalletClient.setDelegate(this)
    }

    override fun onPushRequest(pushRequest: Push.Wallet.Model.Request) {

    }

    override fun onPushMessage(message: Push.Wallet.Model.Message) {

    }
}