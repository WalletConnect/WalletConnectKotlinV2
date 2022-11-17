package com.walletconnect.android.pairing.handler

import com.walletconnect.android.Core
import com.walletconnect.android.pairing.engine.domain.PairingEngine
import com.walletconnect.android.pairing.model.mapper.toAppMetaData
import com.walletconnect.foundation.common.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.merge

internal object PairingHandler : PairingHandlerInterface {
    private lateinit var pairingEngine: PairingEngine
    override val topicExpiredFlow: SharedFlow<Topic> by lazy { pairingEngine.topicExpiredFlow }
    override val findWrongMethodsFlow: Flow<InternalError> by lazy { merge(pairingEngine.internalErrorFlow, pairingEngine.jsonRpcErrorFlow) }

    internal fun initialize(pairingEngine: PairingEngine) {
        this.pairingEngine = pairingEngine
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

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::pairingEngine.isInitialized) {
            "CoreClient needs to be initialized first using the initialize function"
        }
    }
}