package com.walletconnect.web3.modal.ui.routes.connect

import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.toModel
import com.walletconnect.web3.modal.engine.Web3ModalEngine

internal interface ParingController {

    fun connect(
        name: String, method: String,
        sessionParams: Modal.Params.SessionParams,
        onSuccess: (uri: String) -> Unit,
        onError: (Throwable) -> Unit
    )

    fun authenticate(
        name: String, method: String,
        authParams: Modal.Model.AuthPayloadParams,
        walletAppLink: String? = null,
        onSuccess: (String) -> Unit,
        onError: (Throwable) -> Unit
    )

    val uri: String
}

internal class PairingControllerImpl : ParingController {

    private val web3ModalEngine: Web3ModalEngine = wcKoinApp.koin.get()

    private var _pairing: Core.Model.Pairing? = null

    private val pairing: Core.Model.Pairing
        get() = _pairing ?: generatePairing()

    override fun connect(
        name: String, method: String,
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
            web3ModalEngine.connectWC(
                name = name, method = method,
                connect = connectParams,
                onSuccess = onSuccess,
                onError = onError
            )
        } catch (e: Exception) {
            onError(e)
        }
    }

    override fun authenticate(
        name: String,
        method: String,
        authParams: Modal.Model.AuthPayloadParams,
        walletAppLink: String?,
        onSuccess: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            generateAuthenticatedPairing()
            web3ModalEngine.authenticate(
                name = name,
                method = method,
                authenticate = authParams.toModel(pairing.topic),
                walletAppLink = walletAppLink,
                onSuccess = onSuccess,
                onError = onError
            )
        } catch (e: Exception) {
            onError(e)
        }
    }

    override val uri: String
        get() = pairing.uri

    private fun generatePairing(): Core.Model.Pairing =
        CoreClient.Pairing.create { error -> throw IllegalStateException("Creating Pairing failed: ${error.throwable.stackTraceToString()}") }!!.also { _pairing = it }


    private fun generateAuthenticatedPairing(): Core.Model.Pairing =
        CoreClient.Pairing
            .create(methods = "wc_sessionAuthenticate", onError = { error -> throw IllegalStateException("Creating Pairing failed: ${error.throwable.stackTraceToString()}") })!!
            .also { _pairing = it }

}
