package com.walletconnect.android.relay

import com.walletconnect.android.exception.WalletConnectException
import com.walletconnect.foundation.network.RelayInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RelayConnectionInterface : RelayInterface {
    val isConnectionAvailable: StateFlow<Boolean>
    val initializationErrorsFlow: Flow<WalletConnectException>

    fun connect(onError: (String) -> Unit)
    fun disconnect(onError: (String) -> Unit)
}