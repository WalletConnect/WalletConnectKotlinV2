package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Namespace
import com.walletconnect.android.internal.common.model.Participant
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.CoreSignParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.android.internal.common.signing.cacao.Issuer
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.dayInSeconds
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.MissingSessionAuthenticateRequest
import com.walletconnect.sign.common.exceptions.PeerError
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionAuthenticateRequest
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class ApproveSessionAuthenticateUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val getPendingSessionAuthenticateRequest: GetPendingSessionAuthenticateRequest,
    private val crypto: KeyManagementRepository,
    private val cacaoVerifier: CacaoVerifier,
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val logger: Logger,
    private val pairingController: PairingControllerInterface,
    private val selfAppMetaData: AppMetaData,
    private val sessionStorageRepository: SessionStorageRepository
) : ApproveSessionAuthenticateUseCaseInterface {
    override suspend fun approveSessionAuthenticate(id: Long, cacaos: List<Cacao>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        try {
            val jsonRpcHistoryEntry = getPendingSessionAuthenticateRequest(id)

            if (jsonRpcHistoryEntry == null) {
                logger.error(MissingSessionAuthenticateRequest().message)
                onFailure(MissingSessionAuthenticateRequest())
                return@supervisorScope
            }
            //todo: expiry check

            val sessionAuthenticateParams: SignParams.SessionAuthenticateParams = jsonRpcHistoryEntry.params
            val receiverPublicKey = PublicKey(sessionAuthenticateParams.requester.publicKey)
            val receiverMetadata = sessionAuthenticateParams.requester.metadata
            val senderPublicKey: PublicKey = crypto.generateAndStoreX25519KeyPair()
            val symmetricKey: SymmetricKey = crypto.generateSymmetricKeyFromKeyAgreement(senderPublicKey, receiverPublicKey)
            val responseTopic: Topic = crypto.getTopicFromKey(receiverPublicKey)
            val sessionTopic = crypto.getTopicFromKey(symmetricKey)
            val irnParams = IrnParams(Tags.SESSION_AUTHENTICATE_RESPONSE, Ttl(dayInSeconds))

            if (cacaos.find { cacao -> !cacaoVerifier.verify(cacao) } != null) {
                logger.error("Invalid Cacao")
                //todo: handle error codes
                jsonRpcInteractor.respondWithError(id,
                    responseTopic,
                    PeerError.EIP1193.UserRejectedRequest("Invalid CACAO"),
                    irnParams,
                    EnvelopeType.ONE,
                    Participants(senderPublicKey, receiverPublicKey),
                    onSuccess = { return@respondWithError onFailure(Throwable("Invalid CACAO error successfully sent on topic: $responseTopic")) },
                    onFailure = { logger.error("Error sending invalid CACAO error on topic: $responseTopic") })
                return@supervisorScope
            }

            val accounts = cacaos.map { cacao -> Issuer(cacao.payload.iss).accountId }
            val chains = cacaos.map { cacao -> Issuer(cacao.payload.iss).chainId }
            val namespace = Issuer(cacaos.first().payload.iss).namespace
            val methods = cacaos.map { cacao -> cacao.payload.methods }.flatten().distinct()
            val sessionNamespaces: Map<String, Namespace.Session> = mapOf(namespace to Namespace.Session(accounts = accounts, events = listOf(), methods = methods, chains = chains))
            val requiredNamespace: Map<String, Namespace.Proposal> = mapOf(namespace to Namespace.Proposal(events = listOf(), methods = methods, chains = chains))
            val authenticatedSession = SessionVO.createAuthenticatedSession(
                sessionTopic = sessionTopic,
                peerPublicKey = receiverPublicKey,
                peerMetadata = receiverMetadata,
                selfPublicKey = senderPublicKey,
                selfMetadata = selfAppMetaData,
                controllerKey = senderPublicKey,
                requiredNamespaces = requiredNamespace,
                sessionNamespaces = sessionNamespaces,
                pairingTopic = jsonRpcHistoryEntry.topic.value
            )
            sessionStorageRepository.insertSession(authenticatedSession, id)

            val responseParams = CoreSignParams.SessionAuthenticateApproveParams(responder = Participant(publicKey = senderPublicKey.keyAsHex, metadata = selfAppMetaData), cacaos = cacaos)
            val response: JsonRpcResponse = JsonRpcResponse.JsonRpcResult(id, result = responseParams)
            crypto.setKey(symmetricKey, sessionTopic.value)
            jsonRpcInteractor.subscribe(sessionTopic) { error -> onFailure(error) }

            jsonRpcInteractor.publishJsonRpcResponse(responseTopic, irnParams, response, envelopeType = EnvelopeType.ONE, participants = Participants(senderPublicKey, receiverPublicKey),
                onSuccess = {
                    logger.log("Session Authenticate Approve Responded on topic: $responseTopic")
                    scope.launch {
                        supervisorScope {
                            pairingController.activate(Core.Params.Activate(jsonRpcHistoryEntry.topic.value))
                            verifyContextStorageRepository.delete(id)
                        }
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    //todo remove session and keys
                    logger.error("Error Responding Session Authenticate on topic: $responseTopic, error: $error")
                    onFailure(error)
                }
            )
        } catch (e: Exception) {
            //todo remove session and keys
            onFailure(e)
        }
    }
}

internal interface ApproveSessionAuthenticateUseCaseInterface {

    suspend fun approveSessionAuthenticate(id: Long, cacaos: List<Cacao>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}