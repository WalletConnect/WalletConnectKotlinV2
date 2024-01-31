package com.walletconnect.sign.engine.use_case.responses

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.CoreSignParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.android.internal.utils.monthInSeconds
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.utils.toClient
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.engine.model.EngineDO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnSessionAuthenticateResponseUseCase(
    private val pairingController: PairingControllerInterface,
    private val pairingInterface: PairingInterface,
    private val cacaoVerifier: CacaoVerifier,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse, params: SignParams.SessionAuthenticateParams) = supervisorScope {
        try {
            val pairingTopic = wcResponse.topic
            if (!pairingInterface.getPairings().any { pairing -> pairing.topic == pairingTopic.value }) return@supervisorScope
//            todo: handle pending session authenticate requests
//            pairingTopicToResponseTopicMap.remove(pairingTopic)

            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcError -> _events.emit(EngineDO.SessionAuthenticateResponse.Error(response.id, response.error.code, response.error.message))
                is JsonRpcResponse.JsonRpcResult -> {
                    updatePairing(pairingTopic, params)
                    val params = (response.result as CoreSignParams.SessionAuthenticateApproveParams)

                    params.caip222Response.forEach { cacao ->
                        if (!cacaoVerifier.verify(cacao)) {
                            _events.emit(EngineDO.SessionAuthenticateResponse.Error(response.id, 1111, "Message")) //todo: handle errors
                            return@supervisorScope
                        }
                    }

                    _events.emit(EngineDO.SessionAuthenticateResponse.Result(response.id, params.caip222Response)) //todo: add Participant?
                }
            }
        } catch (e: Exception) {
            _events.emit(SDKError(e))
        }
    }

    private fun updatePairing(topic: Topic, requestParams: SignParams.SessionAuthenticateParams) = with(pairingController) {
        updateExpiry(Core.Params.UpdateExpiry(topic.value, Expiry(monthInSeconds)))
        updateMetadata(Core.Params.UpdateMetadata(topic.value, requestParams.requester.metadata.toClient(), AppMetaDataType.PEER))
        activate(Core.Params.Activate(topic.value))
    }
}