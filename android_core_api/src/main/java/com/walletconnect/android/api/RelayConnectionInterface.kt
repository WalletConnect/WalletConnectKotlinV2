package com.walletconnect.android.api

import com.walletconnect.foundation.network.RelayInterface

interface RelayConnectionInterface : RelayInterface {

    fun connect(onError: (String) -> Unit)
    fun disconnect(onError: (String) -> Unit)
}