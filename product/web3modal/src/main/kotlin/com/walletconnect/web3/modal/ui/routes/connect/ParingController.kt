package com.walletconnect.web3.modal.ui.routes.connect

import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal

internal interface ParingController {

    fun connect(
        sessionParams: Modal.Params.SessionParams,
        onSuccess: (uri: String) -> Unit,
        onError: (Throwable) -> Unit
    )

    val uri: String
}

internal class PairingControllerImpl: ParingController {

    private var _pairing: Core.Model.Pairing? = null

    private val pairing: Core.Model.Pairing
        get() = _pairing ?: generatePairing()

    override fun connect(
        sessionParams: Modal.Params.SessionParams,
        onSuccess: (uri: String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            generatePairing()
            val connectParams = Modal.Params.Connect(
                sessionParams.requiredNamespaces,
                sessionParams.optionalNamespaces,
                sessionParams.properties,
                pairing
            )
            Web3Modal.connect(
                connect = connectParams,
                onSuccess = { onSuccess(pairing.uri) },
                onError = { onError(it.throwable) }
            )
        } catch (e: Exception) {
            onError(e)
        }
    }

    override val uri: String
        get() = pairing.uri

    private fun generatePairing() = CoreClient.Pairing.create { error ->
        throw IllegalStateException("Creating Pairing failed: ${error.throwable.stackTraceToString()}")
    }!!.also { _pairing = it }
}
