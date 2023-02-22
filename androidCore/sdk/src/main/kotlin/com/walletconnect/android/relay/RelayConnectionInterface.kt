package com.walletconnect.android.relay

import com.walletconnect.android.Core
import com.walletconnect.foundation.network.RelayInterface
import kotlinx.coroutines.flow.StateFlow

interface RelayConnectionInterface : RelayInterface {
    val isConnectionAvailable: StateFlow<Boolean>

    @Deprecated("This has become deprecate in favor of the onError returning Core.Model.Error", ReplaceWith("this.connect(onErrorModel)"))
    fun connect(onErrorModel: (Core.Model.Error) -> Unit = {}, onError: (String) -> Unit)
    fun connect(onError: (Core.Model.Error) -> Unit)
    @Deprecated("This has become deprecate in favor of the onError returning Core.Model.Error", ReplaceWith("this.disconnect(onErrorModel)"))
    fun disconnect(onErrorModel: (Core.Model.Error) -> Unit = {}, onError: (String) -> Unit)
    fun disconnect(onError: (Core.Model.Error) -> Unit)
}