@file:JvmSynthetic

package com.walletconnect.android.pairing.client

import com.walletconnect.android.Core
import com.walletconnect.android.internal.Validator
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.pairing.engine.domain.PairingEngine
import com.walletconnect.android.pairing.engine.model.EngineDO
import com.walletconnect.android.pairing.model.mapper.toCore
import com.walletconnect.android.pulse.domain.InsertTelemetryEventUseCase
import com.walletconnect.android.relay.RelayConnectionInterface
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.flow.*
import org.koin.core.KoinApplication

internal class PairingProtocol(private val koinApp: KoinApplication = wcKoinApp) : PairingInterface {
    private lateinit var pairingEngine: PairingEngine
    private val logger: Logger by lazy { koinApp.koin.get() }
    private val relayClient: RelayConnectionInterface by lazy { koinApp.koin.get() }
    private val insertEventUseCase: InsertTelemetryEventUseCase by lazy { koinApp.koin.get() }

    override fun initialize() {
        pairingEngine = koinApp.koin.get()
    }

    override fun setDelegate(delegate: PairingInterface.Delegate) {
        checkEngineInitialization()

        pairingEngine.engineEvent.onEach { event ->
            when (event) {
                is EngineDO.PairingDelete -> delegate.onPairingDelete(event.toCore())
                is EngineDO.PairingExpire -> delegate.onPairingExpired(Core.Model.ExpiredPairing(event.pairing.toCore()))
                is EngineDO.PairingState -> delegate.onPairingState(Core.Model.PairingState(event.isPairingState))
            }
        }.launchIn(scope)
    }

    @Throws(IllegalStateException::class)
    override fun create(onError: (Core.Model.Error) -> Unit): Core.Model.Pairing? {
        checkEngineInitialization()

        return try {
            pairingEngine.create({ error -> onError(Core.Model.Error(error)) })
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
            null
        }
    }

    @Throws(IllegalStateException::class)
    override fun create(onError: (Core.Model.Error) -> Unit, methods: String): Core.Model.Pairing? {
        checkEngineInitialization()

        return try {
            pairingEngine.create({ error -> onError(Core.Model.Error(error)) }, methods)
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
            null
        }
    }

    @Throws(IllegalStateException::class)
    override fun pair(
        pair: Core.Params.Pair,
        onSuccess: (Core.Params.Pair) -> Unit,
        onError: (Core.Model.Error) -> Unit,
    ) {
        checkEngineInitialization()
        try {
            pairingEngine.pair(
                uri = pair.uri,
                onSuccess = { onSuccess(pair) },
                onFailure = { error -> onError(Core.Model.Error(Throwable("Pairing error: ${error.message}"))) }
            )
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
        }
    }

    @Deprecated(message = "Disconnect method has been deprecated. It will be removed soon. Pairing will disconnect automatically internally.")
    @Throws(IllegalStateException::class)
    override fun disconnect(disconnect: Core.Params.Disconnect, onError: (Core.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            pairingEngine.disconnect(disconnect.topic) { error -> onError(Core.Model.Error(error)) }
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
        }
    }

    @Deprecated(message = "Disconnect method has been deprecated. It will be removed soon. Pairing will disconnect automatically internally.")
    @Throws(IllegalStateException::class)
    override fun disconnect(topic: String, onError: (Core.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            pairingEngine.disconnect(topic) { error -> onError(Core.Model.Error(error)) }
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
        }
    }

    @Deprecated(message = "Ping method has been deprecated. It will be removed soon. Please use Ping from Web3Wallet or Sign clients.")
    @Throws(IllegalStateException::class)
    override fun ping(ping: Core.Params.Ping, pairingPing: Core.Listeners.PairingPing?) {
        checkEngineInitialization()

        try {
            pairingEngine.ping(ping.topic,
                onSuccess = { topic -> pairingPing?.onSuccess(Core.Model.Ping.Success(topic)) },
                onFailure = { error -> pairingPing?.onError(Core.Model.Ping.Error(error)) })
        } catch (e: Exception) {
            pairingPing?.onError(Core.Model.Ping.Error(e))
        }
    }

    @Throws(IllegalStateException::class)
    override fun getPairings(): List<Core.Model.Pairing> {
        checkEngineInitialization()

        return pairingEngine.getPairings().map { pairing -> pairing.toCore() }
    }

    override fun validatePairingUri(uri: String): Boolean {
        return try {
            Validator.validateWCUri(uri) != null
        } catch (e: Exception) {
            false
        }
    }

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::pairingEngine.isInitialized) {
            "CoreClient needs to be initialized first using the initialize function"
        }
    }
}