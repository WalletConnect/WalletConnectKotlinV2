package com.walletconnect.sign.engine.use_case.calls

import android.database.sqlite.SQLiteException
import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.ACTIVE_SESSION
import com.walletconnect.android.internal.utils.CoreValidator.isExpired
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.InvalidNamespaceException
import com.walletconnect.sign.common.exceptions.SessionProposalExpiredException
import com.walletconnect.sign.common.model.vo.clientsync.common.SessionParticipant
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.proposal.ProposalVO
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.common.validator.SignValidator
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toMapOfNamespacesVOSession
import com.walletconnect.sign.engine.model.mapper.toSessionApproveParams
import com.walletconnect.sign.engine.model.mapper.toSessionProposeRequest
import com.walletconnect.sign.engine.model.mapper.toSessionSettleParams
import com.walletconnect.sign.storage.proposal.ProposalStorageRepository
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class ApproveSessionUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val sessionStorageRepository: SessionStorageRepository,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val selfAppMetaData: AppMetaData,
    private val pairingController: PairingControllerInterface,
    private val logger: Logger
) : ApproveSessionUseCaseInterface {

    override suspend fun approve(
        proposerPublicKey: String,
        sessionNamespaces: Map<String, EngineDO.Namespace.Session>,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) = supervisorScope {
        fun sessionSettle(requestId: Long, proposal: ProposalVO, sessionTopic: Topic, pairingTopic: Topic) {
            val selfPublicKey = crypto.getSelfPublicFromKeyAgreement(sessionTopic)
            val selfParticipant = SessionParticipant(selfPublicKey.keyAsHex, selfAppMetaData)
            val sessionExpiry = ACTIVE_SESSION
            val unacknowledgedSession = SessionVO.createUnacknowledgedSession(sessionTopic, proposal, selfParticipant, sessionExpiry, sessionNamespaces, pairingTopic.value)

            try {
                sessionStorageRepository.insertSession(unacknowledgedSession, requestId)
                metadataStorageRepository.insertOrAbortMetadata(sessionTopic, selfAppMetaData, AppMetaDataType.SELF)
                metadataStorageRepository.insertOrAbortMetadata(sessionTopic, proposal.appMetaData, AppMetaDataType.PEER)
                val params = proposal.toSessionSettleParams(selfParticipant, sessionExpiry, sessionNamespaces)
                val sessionSettle = SignRpc.SessionSettle(params = params)
                val irnParams = IrnParams(Tags.SESSION_SETTLE, Ttl(fiveMinutesInSeconds))

                logger.log("Sending session settle on topic: $sessionTopic")
                jsonRpcInteractor.publishJsonRpcRequest(
                    topic = sessionTopic,
                    params = irnParams, sessionSettle,
                    onSuccess = {
                        onSuccess()
                        scope.launch {
                            supervisorScope {
                                pairingController.activate(Core.Params.Activate(pairingTopic.value))
                                proposalStorageRepository.deleteProposal(proposerPublicKey)
                                verifyContextStorageRepository.delete(proposal.requestId)
                                logger.log("Session settle sent successfully on topic: $sessionTopic")
                            }
                        }
                    },
                    onFailure = { error ->
                        logger.error("Session settle failure on topic: $sessionTopic, error: $error")
                        onFailure(error)
                    }
                )
            } catch (e: SQLiteException) {
                sessionStorageRepository.deleteSession(sessionTopic)
                logger.error("Session settle failure, error: $e")
                // todo: missing metadata deletion. Also check other try catches
                onFailure(e)
            }
        }

        val proposal = proposalStorageRepository.getProposalByKey(proposerPublicKey)
        val request = proposal.toSessionProposeRequest()
        proposal.expiry?.let {
            if (it.isExpired()) {
                logger.error("Proposal expired on approve, topic: ${proposal.pairingTopic.value}, id: ${proposal.requestId}")
                throw SessionProposalExpiredException("Session proposal expired")
            }
        }

        SignValidator.validateSessionNamespace(sessionNamespaces.toMapOfNamespacesVOSession(), proposal.requiredNamespaces) { error ->
            logger.log("Session approve failure - invalid namespaces, error: $error")
            throw InvalidNamespaceException(error.message)
        }

        val selfPublicKey: PublicKey = crypto.generateAndStoreX25519KeyPair()
        val sessionTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, PublicKey(proposerPublicKey))
        val approvalParams = proposal.toSessionApproveParams(selfPublicKey)
        val irnParams = IrnParams(Tags.SESSION_PROPOSE_RESPONSE, Ttl(fiveMinutesInSeconds))
        logger.log("Subscribing to session topic: $sessionTopic")
        jsonRpcInteractor.subscribe(sessionTopic,
            onSuccess = {
                logger.log("Successfully subscribed to session topic: $sessionTopic")
            },
            onFailure = { error ->
                logger.error("Subscribe to session topic failure: $error")
                onFailure(error)
            })
        logger.log("Sending session approve, topic: $sessionTopic")
        jsonRpcInteractor.respondWithParams(request, approvalParams, irnParams,
            onSuccess = {
                logger.log("Session approve sent successfully, topic: $sessionTopic")
            },
            onFailure = { error ->
                logger.error("Session approve failure, topic: $sessionTopic: $error")
                onFailure(error)
            })

        sessionSettle(request.id, proposal, sessionTopic, request.topic)
    }
}

internal interface ApproveSessionUseCaseInterface {
    suspend fun approve(
        proposerPublicKey: String,
        sessionNamespaces: Map<String, EngineDO.Namespace.Session>,
        onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
    )
}