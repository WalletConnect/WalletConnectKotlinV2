package com.walletconnect.sign.engine.use_case.responses

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.CoreSignParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.storage.proposal.ProposalStorageRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class OnSessionProposalResponseUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val pairingController: PairingControllerInterface,
    private val pairingInterface: PairingInterface,
    private val crypto: KeyManagementRepository,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val logger: Logger
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse, params: SignParams.SessionProposeParams) = supervisorScope {
        try {
            val pairingTopic = wcResponse.topic
            if (!pairingInterface.getPairings().any { pairing -> pairing.topic == pairingTopic.value }) return@supervisorScope

            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    updatePairing(pairingTopic)
                    logger.log("Session proposal approve received")
                    val selfPublicKey = PublicKey(params.proposer.publicKey)
                    val approveParams = response.result as CoreSignParams.ApprovalParams
                    val responderPublicKey = PublicKey(approveParams.responderPublicKey)
                    val sessionTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, responderPublicKey)
                    jsonRpcInteractor.subscribe(sessionTopic) { error -> scope.launch { _events.emit(SDKError(error)) } }
                }

                is JsonRpcResponse.JsonRpcError -> {
                    logger.log("Session proposal reject received: ${response.error}")
                    proposalStorageRepository.deleteProposal(params.proposer.publicKey)
                    _events.emit(EngineDO.SessionRejected(pairingTopic.value, response.errorMessage))
                }
            }
        } catch (e: Exception) {
            _events.emit(SDKError(e))
        }
    }

    private fun updatePairing(pairingTopic: Topic) = with(pairingController) {
        updateExpiry(Core.Params.UpdateExpiry(pairingTopic.value, Expiry(MONTH_IN_SECONDS)))
        activate(Core.Params.Activate(pairingTopic.value))
    }
}