@file:JvmSynthetic

package com.walletconnect.push.wallet.engine.domain

import com.walletconnect.android.impl.common.SDKError
import com.walletconnect.android.impl.common.model.ConnectionState
import com.walletconnect.android.impl.common.model.type.EngineEvent
import com.walletconnect.android.impl.utils.DAY_IN_SECONDS
import com.walletconnect.android.internal.common.FIVE_MINUTES_IN_SECONDS
import com.walletconnect.android.internal.common.crypto.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.GenericException
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.model.*
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.PeerError
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.PushParams
import com.walletconnect.push.common.model.toEngineDO
import com.walletconnect.push.common.model.toPushResponseParams
import com.walletconnect.push.dapp.json_rpc.JsonRpcMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class WalletEngine(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val pairingInterface: PairingInterface,
    private val pairingHandler: PairingControllerInterface,
    private val logger: Logger,
) {
    private var jsonRpcRequestsJob: Job? = null
    private var internalErrorsJob: Job? = null
    private val _engineEvent: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val engineEvent: SharedFlow<EngineEvent> = _engineEvent.asSharedFlow()
    private val pushRequests: MutableMap<String, WCRequest> = mutableMapOf()

    init {
        pairingHandler.register(
            JsonRpcMethod.WC_PUSH_REQUEST,
            JsonRpcMethod.WC_PUSH_MESSAGE
        )
//        setupSequenceExpiration()
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

                if (jsonRpcRequestsJob == null) {
                    jsonRpcRequestsJob = collectJsonRpcRequests()
                }

                if (internalErrorsJob == null) {
                    internalErrorsJob = collectInternalErrors()
                }
            }
            .launchIn(scope)
    }

    fun approve(proposerPublicKey: String, onSuccess: (Boolean) -> Unit, onError: (Throwable) -> Unit) {
        val proposerRequest = pushRequests[proposerPublicKey]?.also { request ->
            pushRequests.remove(request.topic.value)
        } ?: return onError(GenericException("Unable to find proposer's request"))

        val selfPublicKey = crypto.generateKeyPair()
        val pushTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, PublicKey(proposerPublicKey))
        val approvalParams = selfPublicKey.toPushResponseParams()
        val irnParams = IrnParams(Tags.PUSH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))
        jsonRpcInteractor.subscribe(pushTopic) { error ->
            return@subscribe onError(error)
        }
        jsonRpcInteractor.respondWithParams(proposerRequest, approvalParams, irnParams) { error ->
            return@respondWithParams onError(error)
        }

        onSuccess(true)
    }

    fun reject(proposerPublicKey: String, reason: String, onSuccess: (Boolean) -> Unit, onError: (Throwable) -> Unit) {
        val proposerRequest = pushRequests[proposerPublicKey]?.also { request ->
            pushRequests.remove(request.topic.value)
        } ?: return onError(GenericException("Unable to find proposer's request"))
        val irnParams = IrnParams(Tags.PUSH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))

        jsonRpcInteractor.respondWithError(proposerRequest, PeerError.EIP1193.UserRejectedRequest(reason), irnParams) { error ->
            return@respondWithError onError(error)
        }
    }

    internal fun getListOfSubscriptions(): Map<String, EngineDO.Subscription> {

        return emptyMap()
    }

    private fun collectJsonRpcRequests(): Job =
        jsonRpcInteractor.clientSyncJsonRpc
            .filter { request -> request.params is PushParams }
            .onEach { request ->
                when (val requestParams = request.params) {
                    is PushParams.RequestParams -> onPushRequest(request, requestParams)
                    is PushParams.MessageParams -> onPushMessage(request, requestParams)
                }
            }.launchIn(scope)

    private fun collectInternalErrors(): Job =
        merge(jsonRpcInteractor.internalErrors, pairingHandler.findWrongMethodsFlow)
            .onEach { exception -> _engineEvent.emit(SDKError(exception)) }
            .launchIn(scope)

    private fun onPushRequest(request: WCRequest, params: PushParams.RequestParams) {
        val irnParams = IrnParams(Tags.PUSH_REQUEST_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))

        try {
            pushRequests[params.publicKey] = request

            scope.launch { _engineEvent.emit(params.toEngineDO(request.id)) }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle the push request: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
        }
    }

    private fun onPushMessage(request: WCRequest, params: PushParams.MessageParams) {
        val irnParams = IrnParams(Tags.PUSH_MESSAGE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS))

        try {
            jsonRpcInteractor.respondWithSuccess(request, irnParams)
            scope.launch { _engineEvent.emit(params.toEngineDO()) }
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle the push message: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
        }
    }
}