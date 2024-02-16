package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.InvalidExpiryException
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.ATT_KEY
import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.RECAPS_PREFIX
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.internal.utils.dayInSeconds
import com.walletconnect.android.internal.utils.getParticipantTag
import com.walletconnect.android.internal.utils.oneHourInSeconds
import com.walletconnect.android.pairing.model.mapper.toPairing
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.model.vo.clientsync.common.Requester
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.validator.SignValidator
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toCommon
import com.walletconnect.sign.engine.model.mapper.toMapOfEngineNamespacesOptional
import com.walletconnect.sign.storage.authenticate.AuthenticateResponseTopicRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.bouncycastle.util.encoders.Base64
import org.json.JSONArray
import org.json.JSONObject

internal class SessionAuthenticateUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val selfAppMetaData: AppMetaData,
    private val authenticateResponseTopicRepository: AuthenticateResponseTopicRepository,
    private val proposeSessionUseCase: ProposeSessionUseCaseInterface,
    private val getPairingForSessionAuthenticate: GetPairingForSessionAuthenticateUseCase,
    private val getNamespacesFromReCaps: GetNamespacesFromReCaps,
    private val logger: Logger
) : SessionAuthenticateUseCaseInterface {
    override suspend fun authenticate(
        payloadParams: EngineDO.PayloadParams,
        methods: List<String>?,
        pairingTopic: String?,
        expiry: Expiry?,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        if (payloadParams.chains.isEmpty()) {
            logger.error("Sending session authenticate request error: chains are empty")
            return onFailure(IllegalArgumentException("Chains are empty"))
        }

        if (!CoreValidator.isExpiryWithinBounds(expiry)) {
            logger.error("Sending session authenticate request error: expiry not within bounds")
            return onFailure(InvalidExpiryException())
        }
        val requestExpiry = expiry ?: Expiry(currentTimeInSeconds + oneHourInSeconds)
        val pairing = getPairingForSessionAuthenticate(pairingTopic)

        println("kobe: chains: ${payloadParams.chains}, methods: $methods")

        val optionalNamespaces = getNamespacesFromReCaps(payloadParams.chains, methods ?: emptyList()).toMapOfEngineNamespacesOptional()

        println("kobe: ONM: $optionalNamespaces")

        if (!methods.isNullOrEmpty()) {
            val namespace = SignValidator.getNamespaceKeyFromChainId(payloadParams.chains.first())
            val actionsJsonObject = JSONObject()
            methods.forEach { method -> actionsJsonObject.put("request/$method", JSONArray().put(0, JSONObject())) }
            val recaps = JSONObject().put(ATT_KEY, JSONObject().put(namespace, actionsJsonObject)).toString().replace("\\/", "/")
            println("kobe: Recaps: $recaps")
            val base64Recaps = Base64.toBase64String(recaps.toByteArray(Charsets.UTF_8))
            val reCapsUrl = "$RECAPS_PREFIX$base64Recaps"
            if (payloadParams.resources == null) payloadParams.resources = listOf(reCapsUrl) else payloadParams.resources = payloadParams.resources!! + reCapsUrl
        }

        val requesterPublicKey: PublicKey = crypto.generateAndStoreX25519KeyPair()
        val responseTopic: Topic = crypto.getTopicFromKey(requesterPublicKey)
        val authParams: SignParams.SessionAuthenticateParams =
            SignParams.SessionAuthenticateParams(
                Requester(requesterPublicKey.keyAsHex, selfAppMetaData),
                payloadParams.toCommon(),
                expiryTimestamp = requestExpiry.seconds
            )
        val authRequest: SignRpc.SessionAuthenticate = SignRpc.SessionAuthenticate(params = authParams)
        crypto.setKey(requesterPublicKey, responseTopic.getParticipantTag())

        logger.log("Session authenticate subscribing on topic: $responseTopic")
        jsonRpcInteractor.subscribe(
            responseTopic,
            onSuccess = {
                logger.log("Session authenticate subscribed on topic: $responseTopic")
                scope.launch {
                    authenticateResponseTopicRepository.insertOrAbort(pairing.topic, responseTopic.value)
                }
            },
            onFailure = { error ->
                logger.error("Session authenticate subscribing on topic error: $responseTopic, $error")
                return@subscribe onFailure(error)
            })

        scope.launch {
            val sessionAuthenticateDeferred = publishSessionAuthenticateDeferred(pairing, authRequest, responseTopic, requestExpiry)
            val sessionProposeDeferred = publishSessionProposeDeferred(pairing, optionalNamespaces, responseTopic)

            val sessionAuthenticateResult = async { sessionAuthenticateDeferred }.await()
            val sessionProposeResult = async { sessionProposeDeferred }.await()

            when {
                sessionAuthenticateResult.isSuccess && sessionProposeResult.isSuccess -> onSuccess(pairing.uri)
                sessionAuthenticateResult.isFailure -> onFailure(sessionAuthenticateResult.exceptionOrNull() ?: Throwable("Session authenticate failed"))
                sessionProposeResult.isFailure -> onFailure(sessionProposeResult.exceptionOrNull() ?: Throwable("Session proposal as a fallback failed"))
                else -> onFailure(Throwable("Session authenticate failed, please try again"))
            }
        }
    }

    private suspend fun publishSessionAuthenticateDeferred(
        pairing: Core.Model.Pairing,
        authRequest: SignRpc.SessionAuthenticate,
        responseTopic: Topic,
        requestExpiry: Expiry
    ): Result<Unit> {
        logger.log("Sending session authenticate on topic: ${pairing.topic}")
        val irnParamsTtl = getIrnParamsTtl(requestExpiry, currentTimeInSeconds)
        val irnParams = IrnParams(Tags.SESSION_AUTHENTICATE, irnParamsTtl, true)
        val sessionAuthenticateDeferred = CompletableDeferred<Result<Unit>>()
        jsonRpcInteractor.publishJsonRpcRequest(Topic(pairing.topic), irnParams, authRequest,
            onSuccess = {
                logger.log("Session authenticate sent successfully on topic: ${pairing.topic}")
                sessionAuthenticateDeferred.complete(Result.success(Unit))
            },
            onFailure = { error ->
                jsonRpcInteractor.unsubscribe(responseTopic)
                logger.error("Failed to send a auth request: $error")
                sessionAuthenticateDeferred.complete(Result.failure(error))
            }
        )
        return sessionAuthenticateDeferred.await()
    }

    private suspend fun publishSessionProposeDeferred(
        pairing: Core.Model.Pairing,
        optionalNamespaces: Map<String, EngineDO.Namespace.Proposal>,
        responseTopic: Topic
    ): Result<Unit> {
        logger.log("Sending session proposal as a fallback on topic: ${pairing.topic}")
        val sessionProposeDeferred = CompletableDeferred<Result<Unit>>()
        proposeSessionUseCase.proposeSession(
            emptyMap(),
            optionalNamespaces,
            properties = null,
            pairing = pairing.toPairing(),
            onSuccess = {
                logger.log("Session proposal as a fallback sent successfully on topic: ${pairing.topic}")
                sessionProposeDeferred.complete(Result.success(Unit))
            },
            onFailure = { error ->
                jsonRpcInteractor.unsubscribe(responseTopic)
                logger.error("Failed to send a session proposal as a fallback: $error")
                sessionProposeDeferred.complete(Result.failure(error))
            }
        )
        return sessionProposeDeferred.await()
    }

    private fun getIrnParamsTtl(expiry: Expiry?, nowInSeconds: Long) = expiry?.run {
        val defaultTtl = dayInSeconds
        val extractedTtl = seconds - nowInSeconds
        val newTtl = extractedTtl.takeIf { extractedTtl >= defaultTtl } ?: defaultTtl
        Ttl(newTtl)
    } ?: Ttl(dayInSeconds)
}

internal interface SessionAuthenticateUseCaseInterface {
    suspend fun authenticate(payloadParams: EngineDO.PayloadParams, methods: List<String>?, pairingTopic: String?, expiry: Expiry?, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit)
}