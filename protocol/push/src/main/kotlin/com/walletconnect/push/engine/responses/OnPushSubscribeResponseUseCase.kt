package com.walletconnect.push.engine.responses

import android.content.res.Resources
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.calcExpiry
import com.walletconnect.push.common.data.storage.SubscriptionRepository
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.toDb
import com.walletconnect.push.engine.domain.EnginePushSubscriptionNotifier
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class OnPushSubscribeResponseUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val enginePushSubscriptionNotifier: EnginePushSubscriptionNotifier,
    private val logger: Logger,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse) = supervisorScope {
        try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val requestedSubscription: EngineDO.Subscription.Requested = subscriptionRepository.getRequestedSubscriptionByRequestId(response.id)
                        ?: return@supervisorScope _events.emit(SDKError(Resources.NotFoundException("Cannot find subscription for topic: ${wcResponse.topic.value}")))

                    // TODO: Add an entry in JsonRpcResultAdapter and create data class for response
                    val dappGeneratedPublicKey = PublicKey((((wcResponse.response as JsonRpcResponse.JsonRpcResult).result as Map<*, *>)["publicKey"] as String))
                    val selfPublicKey: PublicKey = crypto.getSelfPublicFromKeyAgreement(requestedSubscription.responseTopic)
                    val pushTopic: Topic = crypto.generateTopicFromKeyAgreement(selfPublicKey, dappGeneratedPublicKey)
                    val updatedExpiry: Expiry = calcExpiry()
                    val relayProtocolOptions = RelayProtocolOptions()

                    runCatching {
                        with(requestedSubscription) {
                            subscriptionRepository.insertOrAbortActiveSubscription(
                                account.value,
                                updatedExpiry.seconds,
                                relayProtocolOptions.protocol,
                                relayProtocolOptions.data,
                                mapOfScope.toDb(),
                                dappGeneratedPublicKey.keyAsHex,
                                pushTopic.value,
                                requestedSubscription.requestId
                            )
                        }
                    }.mapCatching {
                        metadataStorageRepository.updateOrAbortMetaDataTopic(requestedSubscription.responseTopic, pushTopic)
                    }.fold(onSuccess = {
                        val activeSubscription = with(requestedSubscription) {
                            val dappMetaData: AppMetaData? = metadataStorageRepository.getByTopicAndType(pushTopic, AppMetaDataType.PEER)

                            EngineDO.Subscription.Active(account, mapOfScope, expiry, dappGeneratedPublicKey, pushTopic, dappMetaData, requestedSubscription.requestId)
                        }

                        jsonRpcInteractor.subscribe(pushTopic) { error ->
                            launch {
                                _events.emit(SDKError(error))
                                cancel()
                            }
                        }

                        jsonRpcInteractor.unsubscribe(requestedSubscription.responseTopic) { error ->
                            launch {
                                _events.emit(SDKError(error))
                                cancel()
                            }
                        }

                        val symKey = crypto.getSymmetricKey(pushTopic.value.lowercase())

                        _events.emit(activeSubscription)
                        enginePushSubscriptionNotifier.newlyRespondedRequestedSubscriptionId.updateAndGet { requestedSubscription.requestId to activeSubscription }
                    }, onFailure = {
                        logger.error(it)
                        return@supervisorScope _events.emit(SDKError(Resources.NotFoundException("Subscription already exists for topic: ${wcResponse.topic.value}")))
                    })
                }

                is JsonRpcResponse.JsonRpcError -> {
                    _events.emit(EngineDO.Subscription.Error(wcResponse.response.id, response.error.message))
                }
            }
        } catch (exception: Exception) {
            _events.emit(SDKError(exception))
        }
    }
}