package com.walletconnect.sign.engine.use_case.responses

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.Namespace
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.CoreSignParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.android.internal.common.signing.cacao.Issuer
import com.walletconnect.android.internal.common.signing.cacao.getChains
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.monthInSeconds
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.utils.toClient
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import com.walletconnect.sign.json_rpc.domain.GetSessionAuthenticateRequest
import com.walletconnect.sign.storage.authenticate.AuthenticateResponseTopicRepository
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class OnSessionAuthenticateResponseUseCase(
    private val pairingController: PairingControllerInterface,
    private val pairingInterface: PairingInterface,
    private val cacaoVerifier: CacaoVerifier,
    private val sessionStorageRepository: SessionStorageRepository,
    private val crypto: KeyManagementRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val authenticateResponseTopicRepository: AuthenticateResponseTopicRepository,
    private val logger: Logger,
    private val getSessionAuthenticateRequest: GetSessionAuthenticateRequest,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse, params: SignParams.SessionAuthenticateParams) = supervisorScope {
        try {
            val jsonRpcHistoryEntry = getSessionAuthenticateRequest(wcResponse.response.id)
            logger.log("Received session authenticate response: ${wcResponse.topic}")

            if (jsonRpcHistoryEntry == null) {
                logger.error("Received session authenticate response - rpc entry doesn't exist: ${wcResponse.topic}")
                return@supervisorScope
            }

            val pairingTopic = jsonRpcHistoryEntry.topic
            if (!pairingInterface.getPairings().any { pairing -> pairing.topic == pairingTopic.value }) return@supervisorScope //todo: emit error
            runCatching { authenticateResponseTopicRepository.delete(pairingTopic.value) }.onFailure {
                logger.error("Received session authenticate response - failed to delete authenticate response topic: ${wcResponse.topic}")
                _events.emit(SDKError(it))
            }

            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcError -> {
                    logger.error("Received session authenticate response - emitting rpc error: ${wcResponse.topic}, ${response.error}")
                    _events.emit(EngineDO.SessionAuthenticateResponse.Error(response.id, response.error.code, response.error.message))
                }

                is JsonRpcResponse.JsonRpcResult -> {
                    updatePairing(pairingTopic, params)

                    val approveParams = (response.result as CoreSignParams.SessionAuthenticateApproveParams)
                    if (approveParams.cacaos.find { cacao -> !cacaoVerifier.verify(cacao) } != null) {
                        _events.emit(SDKError(Throwable("Signature verification failed Session Authenticate")))
                        return@supervisorScope
                    }

                    with(approveParams) {
                        val selfPublicKey = PublicKey(params.requester.publicKey)
                        val peerPublicKey = PublicKey(approveParams.responder.publicKey)
                        val symmetricKey: SymmetricKey = crypto.generateSymmetricKeyFromKeyAgreement(selfPublicKey, peerPublicKey)
                        val sessionTopic: Topic = crypto.getTopicFromKey(symmetricKey)
                        crypto.setKey(symmetricKey, sessionTopic.value)

                        val chains = cacaos.first().payload.resources.getChains().ifEmpty { params.authPayload.chains }
                        val addresses = cacaos.map { cacao -> Issuer(cacao.payload.iss).address }.distinct()
                        val accounts = mutableListOf<String>()
                        chains.forEach { chainId -> addresses.forEach { address -> accounts.add("$chainId:$address") } }
                        val namespace = Issuer(cacaos.first().payload.iss).namespace
                        val methods = cacaos.first().payload.methods
                        println("kobe: wallet methods: $methods")
                        var authenticatedSession: SessionVO? = null
                        if (methods.isNotEmpty()) {
                            val sessionNamespaces: Map<String, Namespace.Session> = mapOf(namespace to Namespace.Session(accounts = accounts, events = listOf(), methods = methods, chains = chains))
                            val requiredNamespace: Map<String, Namespace.Proposal> = mapOf(namespace to Namespace.Proposal(events = listOf(), methods = methods, chains = chains))
                            authenticatedSession = SessionVO.createAuthenticatedSession(
                                sessionTopic = sessionTopic,
                                peerPublicKey = PublicKey(approveParams.responder.publicKey),
                                peerMetadata = approveParams.responder.metadata,
                                selfPublicKey = PublicKey(params.requester.publicKey),
                                selfMetadata = params.requester.metadata,
                                controllerKey = PublicKey(approveParams.responder.publicKey),
                                requiredNamespaces = requiredNamespace,
                                sessionNamespaces = sessionNamespaces,
                                pairingTopic = pairingTopic.value
                            )
                            metadataStorageRepository.insertOrAbortMetadata(sessionTopic, params.requester.metadata, AppMetaDataType.SELF)
                            metadataStorageRepository.insertOrAbortMetadata(sessionTopic, approveParams.responder.metadata, AppMetaDataType.PEER)
                            sessionStorageRepository.insertSession(authenticatedSession, response.id)
                        }

                        jsonRpcInteractor.subscribe(sessionTopic) { error -> scope.launch { _events.emit(SDKError(error)) } }
                        logger.log("Received session authenticate response - emitting rpc result: ${wcResponse.topic}")
                        _events.emit(EngineDO.SessionAuthenticateResponse.Result(response.id, approveParams.cacaos, authenticatedSession?.toEngineDO())) //todo: add Participant?
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Received session authenticate response - exception:$e")
            _events.emit(SDKError(e))
        }
    }

    private fun updatePairing(topic: Topic, requestParams: SignParams.SessionAuthenticateParams) = with(pairingController) {
        updateExpiry(Core.Params.UpdateExpiry(topic.value, Expiry(monthInSeconds)))
        updateMetadata(Core.Params.UpdateMetadata(topic.value, requestParams.requester.metadata.toClient(), AppMetaDataType.PEER))
        activate(Core.Params.Activate(topic.value))
    }
}