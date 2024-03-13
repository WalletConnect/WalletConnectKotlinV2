package com.walletconnect.sign.engine.use_case.requests

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.utils.toClient
import com.walletconnect.android.verify.domain.ResolveAttestationIdUseCase
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.validator.SignValidator
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import com.walletconnect.sign.engine.model.mapper.toPeerError
import com.walletconnect.sign.engine.model.mapper.toVO
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.sign.storage.proposal.ProposalStorageRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class OnSessionProposalUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val resolveAttestationIdUseCase: ResolveAttestationIdUseCase,
    private val pairingController: PairingControllerInterface,
    private val logger: Logger
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, payloadParams: SignParams.SessionProposeParams) = supervisorScope {
        val irnParams = IrnParams(Tags.SESSION_PROPOSE_RESPONSE, Ttl(fiveMinutesInSeconds))
        try {
            if (pairingController.getPairingByTopic(request.topic)?.methods?.contains(JsonRpcMethod.WC_SESSION_AUTHENTICATE) == true) {
                logger.error("Session proposal received error: pairing supports authenticated sessions")
                return@supervisorScope
            }
            logger.log("Session proposal received: ${request.topic}")
            SignValidator.validateProposalNamespaces(payloadParams.requiredNamespaces) { error ->
                logger.error("Session proposal received error: required namespace validation: ${error.message}")
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return@supervisorScope
            }

            SignValidator.validateProposalNamespaces(payloadParams.optionalNamespaces ?: emptyMap()) { error ->
                logger.error("Session proposal received error: optional namespace validation: ${error.message}")
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return@supervisorScope
            }

            payloadParams.properties?.let {
                SignValidator.validateProperties(payloadParams.properties) { error ->
                    logger.error("Session proposal received error: session properties validation: ${error.message}")
                    jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                    return@supervisorScope
                }
            }

            proposalStorageRepository.insertProposal(payloadParams.toVO(request.topic, request.id))
            pairingController.setRequestReceived(Core.Params.RequestReceived(request.topic.value))
            pairingController.updateMetadata(Core.Params.UpdateMetadata(request.topic.value, payloadParams.proposer.metadata.toClient(), AppMetaDataType.PEER))
            val url = payloadParams.proposer.metadata.url
            resolveAttestationIdUseCase(request.id, request.message, url) { verifyContext ->
                val sessionProposalEvent = EngineDO.SessionProposalEvent(proposal = payloadParams.toEngineDO(request.topic), context = verifyContext.toEngineDO())
                logger.log("Session proposal received on topic: ${request.topic} - emitting")
                scope.launch { _events.emit(sessionProposalEvent) }
            }
        } catch (e: Exception) {
            logger.error("Session proposal received error: $e")
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle a session proposal: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
            _events.emit(SDKError(e))
        }
    }
}