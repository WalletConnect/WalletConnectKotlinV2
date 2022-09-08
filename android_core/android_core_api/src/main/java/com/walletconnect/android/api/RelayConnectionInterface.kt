package com.walletconnect.android.api

import com.walletconnect.foundation.network.RelayInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RelayConnectionInterface : RelayInterface {

//    val isWSSConnectionOpened: MutableStateFlow<Boolean>

    val isConnectionAvailable: StateFlow<Boolean>
    val initializationErrorsFlow: Flow<WalletConnectException>

    fun connect(onError: (String) -> Unit)
    fun disconnect(onError: (String) -> Unit)
}