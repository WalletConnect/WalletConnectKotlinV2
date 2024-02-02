package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.ATT_KEY
import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.RECAPS_PREFIX
import com.walletconnect.android.internal.utils.currentTimeInSeconds
import com.walletconnect.android.internal.utils.dayInSeconds
import com.walletconnect.android.internal.utils.getParticipantTag
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
import org.bouncycastle.util.encoders.Base64
import org.json.JSONArray
import org.json.JSONObject

internal class SessionAuthenticateUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val selfAppMetaData: AppMetaData,
    private val logger: Logger
) : SessionAuthenticateUseCaseInterface {
    override suspend fun authenticate(payloadParams: EngineDO.PayloadParams, methods: List<String>?, topic: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
//        if (!CoreValidator.isExpiryWithinBounds(expiry ?: Expiry(300))) {
//            return@supervisorScope onFailure(InvalidExpiryException())
//        }

        //TODO: Multi namespace - throw error, chains validation
        val namespace = SignValidator.getNamespaceKeyFromChainId(payloadParams.chains.first())
        val actionsJsonArray = JSONArray()

        methods?.forEachIndexed { index, method -> actionsJsonArray.put(index, JSONObject().put("request/$method", JSONArray())) }

        val recaps =
            JSONObject().put(
                ATT_KEY,
                JSONObject().put(namespace, actionsJsonArray)
            ).toString().replace("\\/", "/")


        val base64Recaps = Base64.toBase64String(recaps.toByteArray(Charsets.UTF_8))
        val reCapsUrl = "$RECAPS_PREFIX$base64Recaps"
        if (payloadParams.resources == null) payloadParams.resources = listOf(reCapsUrl) else payloadParams.resources!!.toMutableList().add(reCapsUrl)


        val responsePublicKey: PublicKey = crypto.generateAndStoreX25519KeyPair()
        val responseTopic: Topic = crypto.getTopicFromKey(responsePublicKey)
        val authParams: SignParams.SessionAuthenticateParams = SignParams.SessionAuthenticateParams(Requester(responsePublicKey.keyAsHex, selfAppMetaData), payloadParams.toCommon())
        val authRequest: SignRpc.SessionAuthenticate = SignRpc.SessionAuthenticate(params = authParams)
        val irnParamsTtl = getIrnParamsTtl(null, currentTimeInSeconds)
        val irnParams = IrnParams(Tags.SESSION_AUTHENTICATE, irnParamsTtl, true)
        val pairingTopic = Topic(topic)

        //todo: use exp from payload
//        val requestTtlInSeconds = expiry?.run { seconds - nowInSeconds } ?: DAY_IN_SECONDS
        crypto.setKey(responsePublicKey, responseTopic.getParticipantTag())

        jsonRpcInteractor.publishJsonRpcRequest(pairingTopic, irnParams, authRequest,
            onSuccess = {
                logger.error("Session authenticate sent successfully on topic: $pairingTopic")
                try {
                    jsonRpcInteractor.subscribe(responseTopic) { error -> return@subscribe onFailure(error) }
                } catch (e: Exception) {
                    return@publishJsonRpcRequest onFailure(e)
                }

//                pairingTopicToResponseTopicMap[pairingTopic] = responseTopic
                onSuccess()
//                collectPeerResponse(requestTtlInSeconds, authRequest)
            },
            onFailure = { error ->
                logger.error("Failed to send a auth request: $error")
                onFailure(error)
            }
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
    suspend fun authenticate(payloadParams: EngineDO.PayloadParams, methods: List<String>?, topic: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}