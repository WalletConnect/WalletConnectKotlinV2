package com.walletconnect.android.relay

import com.walletconnect.foundation.network.RelayInterface
import kotlinx.coroutines.flow.StateFlow

interface RelayConnectionInterface : RelayInterface {
    val isConnectionAvailable: StateFlow<Boolean>

    fun connect(onError: (String) -> Unit)
    fun disconnect(onError: (String) -> Unit)
}