package com.walletconnect.android.pairing.client

import com.walletconnect.android.Core
import com.walletconnect.foundation.common.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface PairingInterface {
    val topicExpiredFlow: SharedFlow<Topic>
    val findWrongMethodsFlow: Flow<InternalError>

    fun ping(ping: Core.Params.Ping, pairingPing: Core.Listeners.PairingPing? = null)

    fun create(onError: (Core.Model.Error) -> Unit = {}): Core.Model.Pairing?

    fun pair(pair: Core.Params.Pair, onError: (Core.Model.Error) -> Unit = {})

    fun getPairings(): List<Core.Model.Pairing>

    @Deprecated(
        message = "Disconnect method has been replaced",
        replaceWith = ReplaceWith(expression = "disconnect(disconnect: Core.Params.Disconnect, onError: (Core.Model.Error) -> Unit = {})")
    )
    fun disconnect(topic: String, onError: (Core.Model.Error) -> Unit = {})

    fun disconnect(disconnect: Core.Params.Disconnect, onError: (Core.Model.Error) -> Unit = {})

    //  idea: --- I think those below shouldn't be accessible by SDK consumers.
    fun activate(activate: Core.Params.Activate, onError: (Core.Model.Error) -> Unit = {})

    fun updateExpiry(updateExpiry: Core.Params.UpdateExpiry, onError: (Core.Model.Error) -> Unit = {})

    fun updateMetadata(updateMetadata: Core.Params.UpdateMetadata, onError: (Core.Model.Error) -> Unit = {})

    fun register(vararg method: String)

    interface Delegate {
        fun onPairingDelete(deletedPairing: Core.Model.DeletedPairing)
    }
}