package com.walletconnect.web3.modal.ui.routes.connect

import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.engine.Web3ModalEngine

internal interface ParingController {

    fun connect(
        name: String, method: String,
        sessionParams: Modal.Params.SessionParams,
        onSuccess: (uri: String) -> Unit,
        onError: (Throwable) -> Unit
    )

    val uri: String
}

internal class PairingControllerImpl : ParingController {

    private val web3ModalEngine: Web3ModalEngine = wcKoinApp.koin.get()
    private val logger: Logger = wcKoinApp.koin.get()

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
            CoreClient.Pairing
                .create { error -> onError(IllegalStateException("Creating Pairing failed: ${error.throwable.message}")) }!!
                .also { _pairing = it }
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

    override val uri: String
        get() = pairing.uri

    private fun generatePairing(): Core.Model.Pairing =
        CoreClient.Pairing
            .create { error -> logger.error(IllegalStateException("Creating Pairing failed: ${error.throwable.message}")) }!!
            .also { _pairing = it }
}
