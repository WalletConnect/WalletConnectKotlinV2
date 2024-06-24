package com.walletconnect.sign.engine.use_case.calls

import android.util.Base64
import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.InvalidExpiryException
import com.walletconnect.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.ATT_KEY
import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.RECAPS_PREFIX
import com.walletconnect.android.internal.common.signing.cacao.mergeReCaps
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
import com.walletconnect.sign.storage.link_mode.LinkModeStorageRepository
import com.walletconnect.utils.Empty
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.json.JSONArray
import org.json.JSONObject

internal class SessionAuthenticateUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val selfAppMetaData: AppMetaData,
    private val authenticateResponseTopicRepository: AuthenticateResponseTopicRepository,
    private val proposeSessionUseCase: ProposeSessionUseCaseInterface,
    private val getPairingForSessionAuthenticate: GetPairingForSessionAuthenticateUseCase,
    private val getNamespacesFromReCaps: GetNamespacesFromReCaps,
    private val linkModeJsonRpcInteractor: LinkModeJsonRpcInteractorInterface,
    private val linkModeStorageRepository: LinkModeStorageRepository,
    private val logger: Logger
) : SessionAuthenticateUseCaseInterface {
    override suspend fun authenticate(
        authenticate: EngineDO.Authenticate,
        methods: List<String>?,
        pairingTopic: String?,
        expiry: Expiry?,
        walletAppLink: String?,
        onSuccess: (String?) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        if (authenticate.chains.isEmpty()) {
            logger.error("Sending session authenticate request error: chains are empty")
            return onFailure(IllegalArgumentException("Chains are empty"))
        }

        if (!CoreValidator.isExpiryWithinBounds(expiry)) {
            logger.error("Sending session authenticate request error: expiry not within bounds")
            return onFailure(InvalidExpiryException())
        }

        val requestExpiry = expiry ?: Expiry(currentTimeInSeconds + oneHourInSeconds)
        val pairing = getPairingForSessionAuthenticate(pairingTopic)
        val optionalNamespaces = getNamespacesFromReCaps(authenticate.chains, if (methods.isNullOrEmpty()) listOf("personal_sign") else methods).toMapOfEngineNamespacesOptional()
        val externalReCapsJson: String = getExternalReCapsJson(authenticate)
        val signReCapsJson = getSignReCapsJson(methods, authenticate)

        val reCaps = when {
            externalReCapsJson.isNotEmpty() && signReCapsJson.isNotEmpty() -> mergeReCaps(JSONObject(signReCapsJson), JSONObject(externalReCapsJson))
            signReCapsJson.isNotEmpty() -> signReCapsJson
            else -> externalReCapsJson
        }.replace("\\\\/", "/")

        if (reCaps.isNotEmpty()) {
            val base64Recaps = Base64.encodeToString(reCaps.toByteArray(Charsets.UTF_8), Base64.NO_WRAP or Base64.NO_PADDING)
            val reCapsUrl = "$RECAPS_PREFIX$base64Recaps"
            if (authenticate.resources == null) authenticate.resources = listOf(reCapsUrl) else authenticate.resources = authenticate.resources!! + reCapsUrl
        }

        val requesterPublicKey: PublicKey = crypto.generateAndStoreX25519KeyPair()
        val responseTopic: Topic = crypto.getTopicFromKey(requesterPublicKey)
        val authParams: SignParams.SessionAuthenticateParams =
            SignParams.SessionAuthenticateParams(Requester(requesterPublicKey.keyAsHex, selfAppMetaData), authenticate.toCommon(), expiryTimestamp = requestExpiry.seconds)
        val authRequest: SignRpc.SessionAuthenticate = SignRpc.SessionAuthenticate(params = authParams)
        crypto.setKey(requesterPublicKey, responseTopic.getParticipantTag())

        //todo: add metadata checks flag for discovery?
        if (!walletAppLink.isNullOrEmpty() && linkModeStorageRepository.isEnabled(walletAppLink)) {
            try {
                linkModeJsonRpcInteractor.triggerRequest(authRequest, appLink = walletAppLink)
                onSuccess(null)
            } catch (e: Error) {
                onFailure(e)
            }
        } else {
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
                    onFailure(error)
                })

            scope.launch {
                supervisorScope {
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
        }
    }

    private fun getSignReCapsJson(methods: List<String>?, authenticate: EngineDO.Authenticate) =
        if (!methods.isNullOrEmpty()) {
            val namespace = SignValidator.getNamespaceKeyFromChainId(authenticate.chains.first())
            val actionsJsonObject = JSONObject()
            methods.forEach { method -> actionsJsonObject.put("request/$method", JSONArray().put(0, JSONObject())) }
            JSONObject().put(ATT_KEY, JSONObject().put(namespace, actionsJsonObject)).toString().replace("\\/", "/")
        } else String.Empty

    private fun getExternalReCapsJson(authenticate: EngineDO.Authenticate): String = try {
        if (areExternalReCapsNotEmpty(authenticate)) {
            val externalUrn = authenticate.resources!!.last { resource -> resource.startsWith(RECAPS_PREFIX) }
            Base64.decode(externalUrn.removePrefix(RECAPS_PREFIX), Base64.NO_WRAP).toString(Charsets.UTF_8)
        } else {
            String.Empty
        }
    } catch (e: Exception) {
        String.Empty
    }

    private fun areExternalReCapsNotEmpty(authenticate: EngineDO.Authenticate): Boolean =
        authenticate.resources != null && authenticate.resources!!.any { resource -> resource.startsWith(RECAPS_PREFIX) }

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
    suspend fun authenticate(
        authenticate: EngineDO.Authenticate,
        methods: List<String>?,
        pairingTopic: String?,
        expiry: Expiry?,
        walletAppLink: String? = null,
        onSuccess: (String?) -> Unit,
        onFailure: (Throwable) -> Unit
    )
}