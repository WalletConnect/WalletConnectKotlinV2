package com.walletconnect.sign.engine.use_case.responses

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.CoreSignParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.monthInSeconds
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
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
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
            logger.log("Session proposal response received on topic: ${wcResponse.topic}")
            val pairingTopic = wcResponse.topic
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    logger.log("Session proposal approval received on topic: ${wcResponse.topic}")
                    updatePairing(pairingTopic)
                    if (!pairingInterface.getPairings().any { pairing -> pairing.topic == pairingTopic.value }) {
                        logger.error("Session proposal approval received failure on topic: ${wcResponse.topic} - invalid pairing")
                        _events.emit(SDKError(Throwable("Invalid Pairing")))
                        return@supervisorScope
                    }
                    val selfPublicKey = PublicKey(params.proposer.publicKey)
                    val approveParams = response.result as CoreSignParams.ApprovalParams
                    val responderPublicKey = PublicKey(approveParams.responderPublicKey)
                    val sessionTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, responderPublicKey)

                    jsonRpcInteractor.subscribe(sessionTopic,
                        onSuccess = { logger.log("Session proposal approval subscribed on session topic: $sessionTopic") },
                        onFailure = { error ->
                            logger.error("Session proposal approval subscribe error on session topic: $sessionTopic - $error")
                            scope.launch { _events.emit(SDKError(error)) }
                        })
                }

                is JsonRpcResponse.JsonRpcError -> {
                    proposalStorageRepository.deleteProposal(params.proposer.publicKey)
                    logger.log("Session proposal rejection received on topic: ${wcResponse.topic}")
                    _events.emit(EngineDO.SessionRejected(pairingTopic.value, response.errorMessage))
                }
            }
        } catch (e: Exception) {
            logger.error("Session proposal response received failure on topic: ${wcResponse.topic}: $e")
            _events.emit(SDKError(e))
        }
    }

    private fun updatePairing(pairingTopic: Topic) = with(pairingController) {
        updateExpiry(Core.Params.UpdateExpiry(pairingTopic.value, Expiry(monthInSeconds)))
        activate(Core.Params.Activate(pairingTopic.value))
    }
}