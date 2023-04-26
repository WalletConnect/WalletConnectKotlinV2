package com.walletconnect.wallet.domain

import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

object CoreDelegate: CoreClient.CoreDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcCoreEventModels: MutableSharedFlow<Core.Model?> = MutableSharedFlow(1)
    val wcCoreEventModels: SharedFlow<Core.Model?> = _wcCoreEventModels

    init {
        CoreClient.setDelegate(this)
    }

    override fun onPairingDelete(deletedPairing: Core.Model.DeletedPairing) {
        scope.launch {
            _wcCoreEventModels.emit(deletedPairing)
        }
    }
}