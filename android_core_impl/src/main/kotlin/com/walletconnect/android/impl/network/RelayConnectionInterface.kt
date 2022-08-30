package com.walletconnect.android.impl.network

import com.walletconnect.foundation.network.RelayInterface

interface RelayConnectionInterface : RelayInterface {
    fun connect(onError: (String) -> Unit)
    fun disconnect(onError: (String) -> Unit)
}
