package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.ATT_KEY
import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.RECAPS_PREFIX
import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.internal.utils.dayInSeconds
import com.walletconnect.android.internal.utils.getParticipantTag
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
    override suspend fun authenticate(payloadParams: EngineDO.PayloadParams, methods: List<String>?, pairingTopic: String?, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
//        if (!CoreValidator.isExpiryWithinBounds(expiry ?: Expiry(300))) {
//            return@supervisorScope onFailure(InvalidExpiryException())
//        }
        //TODO: Multi namespace - if other than eip155 - throw error, add chains validation
        val pairing = getPairingForSessionAuthenticate(pairingTopic)
        val optionalNamespaces = getNamespacesFromReCaps(payloadParams.chains, methods ?: emptyList()).toMapOfEngineNamespacesOptional()
        val namespace = SignValidator.getNamespaceKeyFromChainId(payloadParams.chains.first())

        val actionsJsonObject = JSONObject()
        methods?.forEach { method -> actionsJsonObject.put("request/$method", JSONArray().put(0, JSONObject())) }
        val recaps = JSONObject().put(ATT_KEY, JSONObject().put(namespace, actionsJsonObject)).toString().replace("\\/", "/")
        val base64Recaps = Base64.toBase64String(recaps.toByteArray(Charsets.UTF_8))
        val reCapsUrl = "$RECAPS_PREFIX$base64Recaps"
        if (payloadParams.resources == null) payloadParams.resources = listOf(reCapsUrl) else payloadParams.resources!!.toMutableList().add(reCapsUrl)
        val requesterPublicKey: PublicKey = crypto.generateAndStoreX25519KeyPair()
        val responseTopic: Topic = crypto.getTopicFromKey(requesterPublicKey)
        val authParams: SignParams.SessionAuthenticateParams = SignParams.SessionAuthenticateParams(Requester(requesterPublicKey.keyAsHex, selfAppMetaData), payloadParams.toCommon())
        val authRequest: SignRpc.SessionAuthenticate = SignRpc.SessionAuthenticate(params = authParams)
        val irnParamsTtl = getIrnParamsTtl(null, currentTimeInSeconds)
        val irnParams = IrnParams(Tags.SESSION_AUTHENTICATE, irnParamsTtl, true)

        //todo: use exp from payload
//        val requestTtlInSeconds = expiry?.run { seconds - nowInSeconds } ?: DAY_IN_SECONDS
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

        logger.log("Sending session authenticate on topic: ${pairing.topic}")
        jsonRpcInteractor.publishJsonRpcRequest(Topic(pairing.topic), irnParams, authRequest,
            onSuccess = {
                logger.log("Session authenticate sent successfully on topic: ${pairing.topic}")
                onSuccess(pairing.uri)
            },
            onFailure = { error ->
                logger.error("Failed to send a auth request: $error")
                onFailure(error)
            }
        )

        proposeSessionUseCase.proposeSession(
            emptyMap(),
            optionalNamespaces,
            properties = null,
            pairing = pairing.toPairing(),
            onSuccess = {/*Success*/ },
            onFailure = { error -> onFailure(error) }
        )
    }

    private fun getIrnParamsTtl(expiry: Expiry?, nowInSeconds: Long) = expiry?.run {
        val defaultTtl = dayInSeconds
        val extractedTtl = seconds - nowInSeconds
        val newTtl = extractedTtl.takeIf { extractedTtl >= defaultTtl } ?: defaultTtl
        Ttl(newTtl)
    } ?: Ttl(dayInSeconds)
}

internal interface SessionAuthenticateUseCaseInterface {
    suspend fun authenticate(payloadParams: EngineDO.PayloadParams, methods: List<String>?, pairingTopic: String?, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit)
}