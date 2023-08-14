package com.walletconnect.sign.engine.use_case.requests

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.FIVE_MINUTES_IN_SECONDS
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.android.verify.domain.ResolveAttestationIdUseCase
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.validator.SignValidator
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import com.walletconnect.sign.engine.model.mapper.toPeerError
import com.walletconnect.sign.engine.model.mapper.toVO
import com.walletconnect.sign.storage.proposal.ProposalStorageRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal class OnSessionProposeUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val serializer: JsonRpcSerializer,
    private val resolveAttestationIdUseCase: ResolveAttestationIdUseCase,
    private val pairingController: PairingControllerInterface,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()


    operator fun invoke(request: WCRequest, payloadParams: SignParams.SessionProposeParams) {
        val irnParams = IrnParams(Tags.SESSION_PROPOSE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
        try {
            SignValidator.validateProposalNamespaces(payloadParams.requiredNamespaces) { error ->
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return
            }

            SignValidator.validateProposalNamespaces(payloadParams.optionalNamespaces ?: emptyMap()) { error ->
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return
            }

            payloadParams.properties?.let {
                SignValidator.validateProperties(payloadParams.properties) { error ->
                    jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                    return
                }
            }

            proposalStorageRepository.insertProposal(payloadParams.toVO(request.topic, request.id))
            pairingController.updateMetadata(Core.Params.UpdateMetadata(request.topic.value, payloadParams.proposer.metadata.toClient(), AppMetaDataType.PEER))
            val url = payloadParams.proposer.metadata.url
            val json = serializer.serialize(SignRpc.SessionPropose(id = request.id, params = payloadParams)) ?: throw Exception("Error serializing session proposal")
            resolveAttestationIdUseCase(request.id, json, url) { verifyContext ->
                val sessionProposalEvent = EngineDO.SessionProposalEvent(proposal = payloadParams.toEngineDO(request.topic), context = verifyContext.toEngineDO())
                scope.launch { _events.emit(sessionProposalEvent) }
            }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle a session proposal: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
            scope.launch { _events.emit(SDKError(e)) }
        }
    }
}