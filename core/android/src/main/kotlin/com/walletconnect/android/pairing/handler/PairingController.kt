package com.walletconnect.android.pairing.handler

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.model.Pairing
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.pairing.engine.domain.PairingEngine
import com.walletconnect.android.pairing.model.mapper.toAppMetaData
import com.walletconnect.foundation.common.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.merge
import org.koin.core.KoinApplication

internal class PairingController(private val koinApp: KoinApplication = wcKoinApp) : PairingControllerInterface {
    private lateinit var pairingEngine: PairingEngine
    override val findWrongMethodsFlow: Flow<SDKError> by lazy { merge(pairingEngine.internalErrorFlow, pairingEngine.jsonRpcErrorFlow) }
    override val storedPairingFlow: SharedFlow<Pair<Topic, MutableList<String>>> by lazy { pairingEngine.storedPairingTopicFlow }
    override val checkVerifyKeyFlow: SharedFlow<Unit> by lazy { pairingEngine.checkVerifyKeyFlow }

    override fun initialize() {
        pairingEngine = koinApp.koin.get()
    }

    @Throws(IllegalStateException::class)
    override fun register(vararg method: String) {
        checkEngineInitialization()

        pairingEngine.register(*method)
    }

    @Throws(IllegalStateException::class)
    override fun getPairingByTopic(topic: Topic): Pairing? {
        checkEngineInitialization()

        return try {
            pairingEngine.getPairingByTopic(topic)
        } catch (e: Exception) {
            null
        }
    }

    @Throws(IllegalStateException::class)
    override fun setRequestReceived(activate: Core.Params.RequestReceived, onError: (Core.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            pairingEngine.setRequestReceived(activate.topic) { error -> onError(Core.Model.Error(error)) }
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
    override fun deleteAndUnsubscribePairing(deletePairing: Core.Params.Delete, onError: (Core.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            pairingEngine.deleteAndUnsubscribePairing(deletePairing.topic)
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