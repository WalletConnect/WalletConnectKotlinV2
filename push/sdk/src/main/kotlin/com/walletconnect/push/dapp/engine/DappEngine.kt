@file:JvmSynthetic

package com.walletconnect.push.dapp.engine

import android.util.Log
import com.walletconnect.android.impl.common.SDKError
import com.walletconnect.android.impl.common.model.ConnectionState
import com.walletconnect.android.impl.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.FIVE_MINUTES_IN_SECONDS
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.KeyManagementRepository
import com.walletconnect.android.internal.common.model.*
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.PushRpc
import com.walletconnect.push.common.JsonRpcMethod
import com.walletconnect.util.generateId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class DappEngine(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val pairingInterface: PairingInterface,
    private val pairingHandler: PairingControllerInterface,
    private val selfAppMetaData: AppMetaData,
    private val logger: Logger,
) {
    private var jsonRpcResponsesJob: Job? = null
    private var internalErrorsJob: Job? = null
    private val pushRequest: MutableMap<String, WCRequest> = mutableMapOf()
    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()

    init {
        pairingHandler.register(
            JsonRpcMethod.WC_PUSH_REQUEST,
            JsonRpcMethod.WC_PUSH_MESSAGE
        )
    }

    fun setup() {
        jsonRpcInteractor.isConnectionAvailable
            .onEach { isAvailable -> _engineEvent.emit(ConnectionState(isAvailable)) }
            .filter { isAvailable: Boolean -> isAvailable }
            .onEach {
                supervisorScope {
                    launch(Dispatchers.IO) {
//                        resubscribeToSubscriptions()
                    }
                }

                if (jsonRpcResponsesJob == null) {
                    jsonRpcResponsesJob = collectJsonRpcResponses()
                }

                if (internalErrorsJob == null) {
                    internalErrorsJob = collectInternalErrors()
                }
            }
            .launchIn(scope)
    }

    fun request(
        pairingTopic: String,
        account: String,
        onSuccess: (Long) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        val selfPublicKey = crypto.generateKeyPair()
        val requestParams = PushParams.RequestParams(selfPublicKey.keyAsHex, selfAppMetaData, account)
        val request = PushRpc.PushRequest(id = generateId(), params = requestParams)
        pushRequest[selfPublicKey.keyAsHex] = WCRequest(Topic(pairingTopic), request.id, request.method, requestParams)
        val irnParams = IrnParams(Tags.PUSH_REQUEST, Ttl(FIVE_MINUTES_IN_SECONDS), true)
//        jsonRpcInteractor.subscribe(Topic(pairingTopic)) { error -> return@subscribe onFailure(error) }

        jsonRpcInteractor.publishJsonRpcRequest(Topic(pairingTopic), irnParams, request,
            onSuccess = {
                logger.log("Push Request sent successfully")
                onSuccess(request.id)
            },
            onFailure = { error ->
                logger.error("Failed to send a push request: $error")
                onFailure(error)
            }
        )
    }

    fun notify(
        pairingTopic: String,
        message: EngineDO.PushMessage,
        onFailure: (Throwable) -> Unit,
    ) {
        val messageParams = PushParams.MessageParams(message.title, message.body, message.icon, message.url)
        val request = PushRpc.PushMessage(id = generateId(), params = messageParams)
        val irnParams = IrnParams(Tags.PUSH_MESSAGE, Ttl(FIVE_MINUTES_IN_SECONDS))

        jsonRpcInteractor.publishJsonRpcRequest(Topic(pairingTopic), irnParams, request,
            onSuccess = {
                logger.log("Push Message sent successfully")
            },
            onFailure = { error ->
                logger.error("Failed to sent push message: ${error.stackTraceToString()}")
                onFailure(error)
            }
        )
    }

    private fun collectJsonRpcResponses(): Job =
        jsonRpcInteractor.peerResponse
            .filter { request -> request.params is PushParams.RequestResponseParams }
            .onEach { request ->
                onPushRequestResponse(request, request.params as PushParams.RequestResponseParams)
            }.launchIn(scope)

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(SDKError(exception)) }
            .launchIn(scope)

    private fun onPushRequestResponse(wcRsponse: WCResponse, params: PushParams.RequestResponseParams) {
        try {
            val pairingTopic = wcRsponse.topic

            when (val response = wcRsponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val selfPublicKey = PublicKey(pushRequest.keys.first()) // use crypto to get key
                    val pushTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, PublicKey(params.publicKey))
                    jsonRpcInteractor.subscribe(pushTopic)
                    Log.e("Talha", "subscribed")
                }
                is JsonRpcResponse.JsonRpcError -> {

                }
            }
        } catch (e: Exception) {
            scope.launch { _engineEvent.emit(SDKError(InternalError(e))) }
        }
    }
}