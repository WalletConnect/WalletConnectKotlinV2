package com.walletconnect.auth.use_case.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.InvalidExpiryException
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.android.internal.utils.getParticipantTag
import com.walletconnect.auth.common.json_rpc.AuthParams
import com.walletconnect.auth.common.json_rpc.AuthRpc
import com.walletconnect.auth.common.model.PayloadParams
import com.walletconnect.auth.common.model.Requester
import com.walletconnect.auth.engine.pairingTopicToResponseTopicMap
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import java.util.Date
import java.util.concurrent.TimeUnit

internal class SendAuthRequestUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val selfAppMetaData: AppMetaData,
    private val logger: Logger
) : SendAuthRequestUseCaseInterface {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    override val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    override suspend fun request(
        payloadParams: PayloadParams,
        expiry: Expiry?,
        topic: String,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) = supervisorScope {
        val nowInSeconds = TimeUnit.SECONDS.convert(Date().time, TimeUnit.SECONDS)
        if (!CoreValidator.isExpiryWithinBounds(expiry ?: Expiry(300))) {
            return@supervisorScope onFailure(InvalidExpiryException())
        }

        val responsePublicKey: PublicKey = crypto.generateAndStoreX25519KeyPair()
        val responseTopic: Topic = crypto.getTopicFromKey(responsePublicKey)

        val authParams: AuthParams.RequestParams = AuthParams.RequestParams(Requester(responsePublicKey.keyAsHex, selfAppMetaData), payloadParams, expiry)
        val authRequest: AuthRpc.AuthRequest = AuthRpc.AuthRequest(params = authParams)
        val irnParamsTtl = expiry?.run {
            val defaultTtl = DAY_IN_SECONDS
            val extractedTtl = seconds - nowInSeconds
            val newTtl = extractedTtl.takeIf { extractedTtl >= defaultTtl } ?: defaultTtl

            Ttl(newTtl)
        } ?: Ttl(DAY_IN_SECONDS)
        val irnParams = IrnParams(Tags.AUTH_REQUEST, irnParamsTtl, true)
        val pairingTopic = Topic(topic)
        val requestTtlInSeconds = expiry?.run { seconds - nowInSeconds } ?: DAY_IN_SECONDS
        crypto.setKey(responsePublicKey, responseTopic.getParticipantTag())

        jsonRpcInteractor.publishJsonRpcRequest(pairingTopic, irnParams, authRequest,
            onSuccess = {
                try {
                    jsonRpcInteractor.subscribe(responseTopic) { error ->
                        return@subscribe onFailure(error)
                    }
                } catch (e: Exception) {
                    return@publishJsonRpcRequest onFailure(e)
                }

                pairingTopicToResponseTopicMap[pairingTopic] = responseTopic
                onSuccess()

                scope.launch {
                    try {
                        withTimeout(TimeUnit.SECONDS.toMillis(requestTtlInSeconds)) {
                            jsonRpcInteractor.peerResponse
                                .filter { response -> response.response.id == authRequest.id }
                                .collect { cancel() }
                        }
                    } catch (e: TimeoutCancellationException) {
                        _events.emit(SDKError(e))
                    }
                }
            },
            onFailure = { error ->
                logger.error("Failed to send a auth request: $error")
                onFailure(error)
            }
        )
    }
}

internal interface SendAuthRequestUseCaseInterface {
    val events: SharedFlow<EngineEvent>
    suspend fun request(payloadParams: PayloadParams, expiry: Expiry? = null, topic: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}