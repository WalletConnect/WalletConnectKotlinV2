package com.walletconnect.android.pairing.client

import com.walletconnect.android.Core

interface PairingInterface {
    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun create(onError: (Core.Model.Error) -> Unit = {}): Core.Model.Pairing?

    fun pair(
        pair: Core.Params.Pair,
        onSuccess: (Core.Params.Pair) -> Unit = {},
        onError: (Core.Model.Error) -> Unit = {}
    )

    @Deprecated(
        message = "Disconnect method has been replaced",
        replaceWith = ReplaceWith(expression = "disconnect(disconnect: Core.Params.Disconnect, onError: (Core.Model.Error) -> Unit = {})")
    )
    fun disconnect(topic: String, onError: (Core.Model.Error) -> Unit = {})

    fun disconnect(disconnect: Core.Params.Disconnect, onError: (Core.Model.Error) -> Unit = {})

    fun ping(ping: Core.Params.Ping, pairingPing: Core.Listeners.PairingPing? = null)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getPairings(): List<Core.Model.Pairing>

    interface Delegate {
        fun onPairingDelete(deletedPairing: Core.Model.DeletedPairing)
    }
}