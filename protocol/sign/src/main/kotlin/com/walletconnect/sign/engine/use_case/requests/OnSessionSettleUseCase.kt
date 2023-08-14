package com.walletconnect.sign.engine.use_case.requests

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.MetadataStorageRepository
import com.walletconnect.android.internal.utils.FIVE_MINUTES_IN_SECONDS
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.sign.common.exceptions.PeerError
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.common.validator.SignValidator
import com.walletconnect.sign.engine.model.mapper.toPeerError
import com.walletconnect.sign.engine.model.mapper.toSessionApproved
import com.walletconnect.sign.storage.proposal.ProposalStorageRepository
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import com.walletconnect.utils.Empty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class OnSessionSettleUseCase(
    private val crypto: KeyManagementRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val sessionStorageRepository: SessionStorageRepository,
    private val pairingController: PairingControllerInterface,
    private val selfAppMetaData: AppMetaData,
    private val metadataStorageRepository: MetadataStorageRepository
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    operator fun invoke(request: WCRequest, settleParams: SignParams.SessionSettleParams) {
        val sessionTopic = request.topic
        val irnParams = IrnParams(Tags.SESSION_SETTLE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))
        val selfPublicKey: PublicKey = try {
            crypto.getSelfPublicFromKeyAgreement(sessionTopic)
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(request, PeerError.Failure.SessionSettlementFailed(e.message ?: String.Empty), irnParams)
            return
        }

        val peerMetadata = settleParams.controller.metadata
        val proposal = try {
            proposalStorageRepository.getProposalByKey(selfPublicKey.keyAsHex).also { proposalStorageRepository.deleteProposal(selfPublicKey.keyAsHex) }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(request, PeerError.Failure.SessionSettlementFailed(e.message ?: String.Empty), irnParams)
            return
        }

        val (requiredNamespaces, optionalNamespaces, properties) = proposal.run { Triple(requiredNamespaces, optionalNamespaces, properties) }
        SignValidator.validateSessionNamespace(settleParams.namespaces, requiredNamespaces) { error ->
            jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
            return
        }

        scope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    val session = SessionVO.createAcknowledgedSession(
                        sessionTopic,
                        settleParams,
                        selfPublicKey,
                        selfAppMetaData,
                        requiredNamespaces,
                        optionalNamespaces,
                        properties,
                        proposal.pairingTopic.value
                    )

                    sessionStorageRepository.insertSession(session, request.id)
                    pairingController.updateMetadata(Core.Params.UpdateMetadata(proposal.pairingTopic.value, peerMetadata.toClient(), AppMetaDataType.PEER))
                    metadataStorageRepository.insertOrAbortMetadata(sessionTopic, peerMetadata, AppMetaDataType.PEER)
                    jsonRpcInteractor.respondWithSuccess(request, irnParams)
                    _events.emit(session.toSessionApproved())
                } catch (e: Exception) {
                    proposalStorageRepository.insertProposal(proposal)
                    sessionStorageRepository.deleteSession(sessionTopic)
                    jsonRpcInteractor.respondWithError(request, PeerError.Failure.SessionSettlementFailed(e.message ?: String.Empty), irnParams)
                    scope.launch { _events.emit(SDKError(e)) }
                    return@supervisorScope
                }
            }
        }
    }
}