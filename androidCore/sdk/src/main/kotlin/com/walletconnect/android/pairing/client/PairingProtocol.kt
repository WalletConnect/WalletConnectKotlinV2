@file:JvmSynthetic

package com.walletconnect.android.pairing.client

import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.pairing.engine.domain.PairingEngine
import com.walletconnect.android.pairing.engine.model.EngineDO
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

internal object PairingProtocol : PairingInterface {
    private lateinit var pairingEngine: PairingEngine
    private val logger: Logger by lazy { wcKoinApp.koin.get() }

    internal fun initialize() {
        pairingEngine = wcKoinApp.koin.get()
    }

    fun setDelegate(delegate: PairingInterface.Delegate) {
        checkEngineInitialization()

        pairingEngine.engineEvent.onEach { event ->
            when (event) {
                is EngineDO.PairingDelete -> delegate.onPairingDelete(event.toClient())
            }
        }.launchIn(scope)
    }

    @Throws(IllegalStateException::class)
    override fun create(onError: (Core.Model.Error) -> Unit): Core.Model.Pairing? {
        checkEngineInitialization()

        return try {
            pairingEngine.create { error -> onError(Core.Model.Error(error)) }
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
            null
        }
    }

    @Throws(IllegalStateException::class)
    override fun pair(
        pair: Core.Params.Pair,
        onSuccess: (Core.Params.Pair) -> Unit,
        onError: (Core.Model.Error) -> Unit
    ) {
        checkEngineInitialization()

        scope.launch(Dispatchers.IO) {
            awaitConnection(
                {
                    try {
                        pairingEngine.pair(
                            uri = pair.uri,
                            onSuccess = { onSuccess(pair) },
                            onFailure = { error -> onError(Core.Model.Error(error)) }
                        )
                    } catch (e: Exception) {
                        onError(Core.Model.Error(e))
                    }
                },
                { throwable ->
                    logger.error(throwable)
                    onError(Core.Model.Error(throwable))
                })
        }
    }

    @Throws(IllegalStateException::class)
    override fun disconnect(disconnect: Core.Params.Disconnect, onError: (Core.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            pairingEngine.disconnect(disconnect.topic) { error -> onError(Core.Model.Error(error)) }
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
        }
    }

    @Throws(IllegalStateException::class)
    override fun disconnect(topic: String, onError: (Core.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            pairingEngine.disconnect(topic) { error -> onError(Core.Model.Error(error)) }
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
        }
    }


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

        return pairingEngine.getPairings().map { pairing -> pairing.toClient() }
    }

    private suspend fun awaitConnection(onConnection: () -> Unit, errorLambda: (Throwable) -> Unit = {}) {
        try {
            withTimeout(5000) {
                while (true) {
                    if (CoreClient.Relay.isConnectionAvailable.value) {
                        onConnection()
                        return@withTimeout
                    }
                    delay(100)
                }
            }
        } catch (e: Exception) {
            errorLambda(e)
        }
    }

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::pairingEngine.isInitialized) {
            "CoreClient needs to be initialized first using the initialize function"
        }
    }
}