@file:JvmSynthetic

package com.walletconnect.android.pairing.client

import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.pairing.engine.domain.PairingEngine
import com.walletconnect.android.pairing.engine.model.EngineDO
import com.walletconnect.android.pairing.model.mapper.toAppMetaData
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.dsl.module

internal object PairingProtocol : PairingInterface {
    private lateinit var pairingEngine: PairingEngine
    private val logger: Logger by lazy { wcKoinApp.koin.get() }
    override val topicExpiredFlow: SharedFlow<Topic> by lazy { pairingEngine.topicExpiredFlow }
    override val findWrongMethodsFlow: Flow<InternalError> by lazy { merge(pairingEngine.internalErrorFlow, pairingEngine.jsonRpcErrorFlow) }

    fun initialize(metaData: Core.Model.AppMetaData) {
        pairingEngine = PairingEngine()

        wcKoinApp.modules(module {
            with(metaData) { single { AppMetaData(name, description, url, icons, Redirect(redirect)) } }
        })
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

        return pairingEngine.create { error -> onError(Core.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun pair(pair: Core.Params.Pair, onError: (Core.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch(Dispatchers.IO) {
            awaitConnection({
                pairingEngine.pair(pair.uri) { error -> onError(Core.Model.Error(error)) }
            }, { throwable ->
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

    @Throws(IllegalStateException::class)
    override fun register(vararg method: String) {
        checkEngineInitialization()

        pairingEngine.register(*method)
    }

    @Throws(IllegalStateException::class)
    override fun activate(activate: Core.Params.Activate, onError: (Core.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            pairingEngine.activate(activate.topic) { error -> onError(Core.Model.Error(error)) }
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
        }
    }

    @Throws(IllegalStateException::class)
    override fun updateExpiry(updateExpiry: Core.Params.UpdateExpiry, onError: (Core.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            pairingEngine.updateExpiry(updateExpiry.topic, updateExpiry.expiry) { error -> onError(Core.Model.Error(error)) }
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
        }
    }

    @Throws(IllegalStateException::class)
    override fun updateMetadata(updateMetadata: Core.Params.UpdateMetadata, onError: (Core.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            pairingEngine.updateMetadata(updateMetadata.topic, updateMetadata.metadata.toAppMetaData(), updateMetadata.metaDataType)
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
        }
    }

    private suspend fun awaitConnection(onConnection: () -> Unit, errorLambda: (Throwable) -> Unit = {}) {
        try {
            withTimeout(2000) {
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