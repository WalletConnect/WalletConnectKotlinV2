package com.walletconnect.auth.use_case.responses

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.CoreAuthParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.auth.common.exceptions.PeerError
import com.walletconnect.auth.common.json_rpc.AuthParams
import com.walletconnect.auth.common.model.AuthResponse
import com.walletconnect.auth.common.model.Events
import com.walletconnect.auth.engine.pairingTopicToResponseTopicMap
import com.walletconnect.foundation.common.model.Topic
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class OnAuthRequestResponseUseCase(
    private val pairingInterface: PairingInterface,
    private val pairingHandler: PairingControllerInterface,
    private val cacaoVerifier: CacaoVerifier,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse, requestParams: AuthParams.RequestParams) = supervisorScope {
        try {
            val pairingTopic = wcResponse.topic
            if (!pairingInterface.getPairings().any { pairing -> pairing.topic == pairingTopic.value }) return@supervisorScope
            pairingTopicToResponseTopicMap.remove(pairingTopic)

            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcError -> _events.emit(Events.OnAuthResponse(response.id, AuthResponse.Error(response.error.code, response.error.message)))
                is JsonRpcResponse.JsonRpcResult -> {
                    updatePairing(pairingTopic, requestParams)
                    val (header, payload, signature) = (response.result as CoreAuthParams.ResponseParams)
                    val cacao = Cacao(header, payload, signature)
                    if (cacaoVerifier.verify(cacao)) {
                        _events.emit(Events.OnAuthResponse(response.id, AuthResponse.Result(cacao)))
                    } else {
                        _events.emit(Events.OnAuthResponse(response.id, AuthResponse.Error(PeerError.SignatureVerificationFailed.code, PeerError.SignatureVerificationFailed.message)))
                    }
                }
            }
        } catch (e: Exception) {
            _events.emit(SDKError(e))
        }
    }

    private fun updatePairing(topic: Topic, requestParams: AuthParams.RequestParams) = with(pairingHandler) {
        updateExpiry(Core.Params.UpdateExpiry(topic.value, Expiry(MONTH_IN_SECONDS)))
        updateMetadata(Core.Params.UpdateMetadata(topic.value, requestParams.requester.metadata.toClient(), AppMetaDataType.PEER))
        activate(Core.Params.Activate(topic.value))
    }
}