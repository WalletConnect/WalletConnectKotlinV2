package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.NoInternetConnectionException
import com.walletconnect.android.internal.common.exception.NoRelayConnectionException
import com.walletconnect.android.internal.common.exception.RequestExpiredException
import com.walletconnect.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Namespace
import com.walletconnect.android.internal.common.model.Participant
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.TransportType
import com.walletconnect.android.internal.common.model.params.CoreSignParams
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.android.internal.common.signing.cacao.Issuer
import com.walletconnect.android.internal.common.signing.cacao.getChains
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.android.internal.utils.CoreValidator.isExpired
import com.walletconnect.android.internal.utils.dayInSeconds
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.pulse.domain.InsertEventUseCase
import com.walletconnect.android.pulse.model.EventType
import com.walletconnect.android.pulse.model.Trace
import com.walletconnect.android.pulse.model.properties.Properties
import com.walletconnect.android.pulse.model.properties.Props
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.MissingSessionAuthenticateRequest
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.common.validator.SignValidator
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionAuthenticateRequest
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class ApproveSessionAuthenticateUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val getPendingSessionAuthenticateRequest: GetPendingSessionAuthenticateRequest,
    private val crypto: KeyManagementRepository,
    private val cacaoVerifier: CacaoVerifier,
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val logger: Logger,
    private val pairingController: PairingControllerInterface,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val selfAppMetaData: AppMetaData,
    private val sessionStorageRepository: SessionStorageRepository,
    private val insertEventUseCase: InsertEventUseCase,
    private val linkModeJsonRpcInteractor: LinkModeJsonRpcInteractorInterface
) : ApproveSessionAuthenticateUseCaseInterface {
    override suspend fun approveSessionAuthenticate(id: Long, cacaos: List<Cacao>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val trace: MutableList<String> = mutableListOf()
        trace.add(Trace.SessionAuthenticate.SESSION_AUTHENTICATE_APPROVE_STARTED).also { logger.log(Trace.SessionAuthenticate.SESSION_AUTHENTICATE_APPROVE_STARTED) }
        try {
            val jsonRpcHistoryEntry = getPendingSessionAuthenticateRequest(id)
            if (jsonRpcHistoryEntry == null) {
                insertEventUseCase(Props(type = EventType.Error.MISSING_SESSION_AUTH_REQUEST, properties = Properties(trace = trace)))
                    .also { logger.error(MissingSessionAuthenticateRequest().message) }
                onFailure(MissingSessionAuthenticateRequest())
                return@supervisorScope
            }

            jsonRpcHistoryEntry.expiry?.let {
                if (it.isExpired()) {
                    insertEventUseCase(Props(type = EventType.Error.SESSION_AUTH_REQUEST_EXPIRED, properties = Properties(trace = trace)))
                        .also { logger.error("Session Authenticate Request Expired: ${jsonRpcHistoryEntry.topic}, id: ${jsonRpcHistoryEntry.id}") }
                    throw RequestExpiredException("This request has expired, id: ${jsonRpcHistoryEntry.id}")
                }
            }
            trace.add(Trace.SessionAuthenticate.AUTHENTICATED_SESSION_NOT_EXPIRED)
            val sessionAuthenticateParams: SignParams.SessionAuthenticateParams = jsonRpcHistoryEntry.params
            val chains = cacaos.first().payload.resources.getChains().ifEmpty { sessionAuthenticateParams.authPayload.chains }
            if (!chains.all { chain -> CoreValidator.isChainIdCAIP2Compliant(chain) }) {
                insertEventUseCase(Props(type = EventType.Error.CHAINS_CAIP2_COMPLIANT_FAILURE, properties = Properties(trace = trace)))
                throw Exception("Chains are not CAIP-2 compliant")
            }
            trace.add(Trace.SessionAuthenticate.CHAINS_CAIP2_COMPLIANT)
            if (!chains.all { chain -> SignValidator.getNamespaceKeyFromChainId(chain) == "eip155" }) {
                insertEventUseCase(Props(type = EventType.Error.CHAINS_EVM_COMPLIANT_FAILURE, properties = Properties(trace = trace)))
                throw Exception("Only eip155 (EVM) is supported")
            }
            trace.add(Trace.SessionAuthenticate.CHAINS_EVM_COMPLIANT)
            val receiverPublicKey = PublicKey(sessionAuthenticateParams.requester.publicKey)
            val receiverMetadata = sessionAuthenticateParams.requester.metadata
            val senderPublicKey: PublicKey = crypto.generateAndStoreX25519KeyPair()
            val symmetricKey: SymmetricKey = crypto.generateSymmetricKeyFromKeyAgreement(senderPublicKey, receiverPublicKey)
            val responseTopic: Topic = crypto.getTopicFromKey(receiverPublicKey)
            val sessionTopic = crypto.getTopicFromKey(symmetricKey)
            trace.add(Trace.SessionAuthenticate.CREATE_AUTHENTICATED_SESSION_TOPIC)
            val irnParams = IrnParams(Tags.SESSION_AUTHENTICATE_RESPONSE_APPROVE, Ttl(dayInSeconds))

            if (cacaos.find { cacao -> !cacaoVerifier.verify(cacao) } != null) {
                insertEventUseCase(Props(type = EventType.Error.INVALID_CACAO, properties = Properties(trace = trace, topic = sessionTopic.value)))
                    .also { logger.error("Invalid Cacao for Session Authenticate") }
                return@supervisorScope onFailure(Throwable("Signature verification failed Session Authenticate, please try again"))
            }
            trace.add(Trace.SessionAuthenticate.CACAOS_VERIFIED)

            val addresses = cacaos.map { cacao -> Issuer(cacao.payload.iss).address }.distinct()
            val accounts = mutableListOf<String>()
            chains.forEach { chainId -> addresses.forEach { address -> accounts.add("$chainId:$address") } }
            val namespace = Issuer(cacaos.first().payload.iss).namespace
            val methods = cacaos.first().payload.methods
            val events = listOf("chainChanged", "accountsChanged")
            if (methods.isNotEmpty()) {
                logger.log("Creating authenticated session")
                val requiredNamespace: Map<String, Namespace.Proposal> = mapOf(namespace to Namespace.Proposal(events = events, methods = methods, chains = chains))
                val sessionNamespaces: Map<String, Namespace.Session> = mapOf(namespace to Namespace.Session(accounts = accounts, events = events, methods = methods, chains = chains))
                val authenticatedSession = SessionVO.createAuthenticatedSession(
                    sessionTopic = sessionTopic,
                    peerPublicKey = receiverPublicKey,
                    peerMetadata = receiverMetadata,
                    selfPublicKey = senderPublicKey,
                    selfMetadata = selfAppMetaData,
                    controllerKey = senderPublicKey,
                    requiredNamespaces = requiredNamespace,
                    sessionNamespaces = sessionNamespaces,
                    pairingTopic = jsonRpcHistoryEntry.topic.value,
                    transportType = jsonRpcHistoryEntry.transportType
                )
                metadataStorageRepository.insertOrAbortMetadata(sessionTopic, selfAppMetaData, AppMetaDataType.SELF)
                metadataStorageRepository.insertOrAbortMetadata(sessionTopic, receiverMetadata, AppMetaDataType.PEER)
                sessionStorageRepository.insertSession(authenticatedSession, id)
                trace.add(Trace.SessionAuthenticate.STORE_AUTHENTICATED_SESSION)
            }

            val responseParams = CoreSignParams.SessionAuthenticateApproveParams(responder = Participant(publicKey = senderPublicKey.keyAsHex, metadata = selfAppMetaData), cacaos = cacaos)
            val response: JsonRpcResponse = JsonRpcResponse.JsonRpcResult(id, result = responseParams)
            crypto.setKey(symmetricKey, sessionTopic.value)
            trace.add(Trace.SessionAuthenticate.SUBSCRIBING_AUTHENTICATED_SESSION_TOPIC).also { logger.log("Subscribing Session Authenticate on topic: $responseTopic") }
            jsonRpcInteractor.subscribe(sessionTopic,
                onSuccess = {
                    trace.add(Trace.SessionAuthenticate.SUBSCRIBE_AUTHENTICATED_SESSION_TOPIC_SUCCESS).also { logger.log("Subscribed Session Authenticate on topic: $responseTopic") }
                },
                onFailure = { error ->
                    scope.launch {
                        supervisorScope { insertEventUseCase(Props(type = EventType.Error.SUBSCRIBE_AUTH_SESSION_TOPIC_FAILURE, properties = Properties(trace = trace, topic = sessionTopic.value))) }
                    }.also { logger.log("Subscribing Session Authenticate error on topic: $responseTopic, $error") }
                    onFailure(error)
                }
            )

            if (jsonRpcHistoryEntry.transportType == TransportType.LINK_MODE) {
                //todo: add success and error callbacks
                linkModeJsonRpcInteractor.triggerResponse(responseTopic, response, Participants(senderPublicKey, receiverPublicKey), EnvelopeType.ONE)
            } else {
                trace.add(Trace.SessionAuthenticate.PUBLISHING_AUTHENTICATED_SESSION_APPROVE).also { logger.log("Sending Session Authenticate Approve on topic: $responseTopic") }
                jsonRpcInteractor.publishJsonRpcResponse(responseTopic, irnParams, response, envelopeType = EnvelopeType.ONE, participants = Participants(senderPublicKey, receiverPublicKey),
                    onSuccess = {
                        trace.add(Trace.SessionAuthenticate.AUTHENTICATED_SESSION_APPROVE_PUBLISH_SUCCESS).also { logger.log("Session Authenticate Approve Responded on topic: $responseTopic") }
                        onSuccess()
                        scope.launch {
                            supervisorScope {
                                pairingController.activate(Core.Params.Activate(jsonRpcHistoryEntry.topic.value))
                                verifyContextStorageRepository.delete(id)
                            }
                        }
                    },
                    onFailure = { error ->
                        runCatching { crypto.removeKeys(sessionTopic.value) }.onFailure { logger.error(it) }
                        sessionStorageRepository.deleteSession(sessionTopic)
                        scope.launch {
                            supervisorScope {
                                insertEventUseCase(Props(type = EventType.Error.AUTHENTICATED_SESSION_APPROVE_PUBLISH_FAILURE, properties = Properties(trace = trace, topic = responseTopic.value)))
                            }
                        }.also { logger.error("Error Responding Session Authenticate on topic: $responseTopic, error: $error") }
                        onFailure(error)
                    }
                )
            }
        } catch (e: Exception) {
            logger.error("Error Responding Session Authenticate, error: $e")
            if (e is NoRelayConnectionException) insertEventUseCase(Props(type = EventType.Error.NO_WSS_CONNECTION, properties = Properties(trace = trace)))
            if (e is NoInternetConnectionException) insertEventUseCase(Props(type = EventType.Error.NO_INTERNET_CONNECTION, properties = Properties(trace = trace)))
            onFailure(e)
        }
    }
}

internal interface ApproveSessionAuthenticateUseCaseInterface {
    suspend fun approveSessionAuthenticate(id: Long, cacaos: List<Cacao>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}