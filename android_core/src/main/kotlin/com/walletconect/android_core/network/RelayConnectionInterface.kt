@file:JvmSynthetic

package com.walletconect.android_core.network

import com.walletconnect.foundation.network.RelayInterface

interface RelayConnectionInterface : RelayInterface {
    fun connect(onError: (String) -> Unit)
    fun disconnect(onError: (String) -> Unit)
}
