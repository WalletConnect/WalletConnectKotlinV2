@file:JvmSynthetic

package com.walletconnect.notify.engine.responses

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
import com.walletconnect.notify.common.calcExpiry
import com.walletconnect.notify.common.model.Error
import com.walletconnect.notify.common.model.Subscription
import com.walletconnect.notify.common.model.toDb
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.engine.domain.EngineNotifySubscriptionNotifier
import com.walletconnect.notify.engine.sync.use_case.requests.SetSubscriptionWithSymmetricKeyToNotifySubscriptionStoreUseCase
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class OnNotifySubscribeResponseUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val engineNotifySubscriptionNotifier: EngineNotifySubscriptionNotifier,
    private val setSubscriptionWithSymmetricKeyToNotifySubscriptionStoreUseCase: SetSubscriptionWithSymmetricKeyToNotifySubscriptionStoreUseCase,
    private val logger: Logger,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse) = supervisorScope {
        try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val requestedSubscription: Subscription.Requested = subscriptionRepository.getRequestedSubscriptionByRequestId(response.id)
                        ?: return@supervisorScope _events.emit(SDKError(Resources.NotFoundException("Cannot find subscription for topic: ${wcResponse.topic.value}")))

                    // TODO: Add an entry in JsonRpcResultAdapter and create data class for response
                    val dappGeneratedPublicKey = PublicKey((((wcResponse.response as JsonRpcResponse.JsonRpcResult).result as Map<*, *>)["publicKey"] as String))
                    val selfPublicKey: PublicKey = crypto.getSelfPublicFromKeyAgreement(requestedSubscription.responseTopic)
                    val notifyTopic: Topic = crypto.generateTopicFromKeyAgreement(selfPublicKey, dappGeneratedPublicKey)
                    val updatedExpiry: Expiry = calcExpiry()
                    val relayProtocolOptions = RelayProtocolOptions()

                    runCatching<Unit> {
                        with(requestedSubscription) {
                            subscriptionRepository.insertOrAbortActiveSubscription(
                                account.value,
                                updatedExpiry.seconds,
                                relayProtocolOptions.protocol,
                                relayProtocolOptions.data,
                                mapOfNotificationScope.toDb(),
                                dappGeneratedPublicKey.keyAsHex,
                                notifyTopic.value,
                                requestedSubscription.requestId
                            )
                        }
                    }.mapCatching {
                        metadataStorageRepository.updateOrAbortMetaDataTopic(requestedSubscription.responseTopic, notifyTopic)
                    }.fold(onSuccess = {
                        val activeSubscription = with(requestedSubscription) {
                            val dappMetaData: AppMetaData? = metadataStorageRepository.getByTopicAndType(notifyTopic, AppMetaDataType.PEER)

                            Subscription.Active(account, mapOfNotificationScope, expiry, dappGeneratedPublicKey, notifyTopic, dappMetaData, requestedSubscription.requestId)
                        }

                        jsonRpcInteractor.subscribe(notifyTopic) { error ->
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

                        val symKey = crypto.getSymmetricKey(notifyTopic.value.lowercase())
                        setSubscriptionWithSymmetricKeyToNotifySubscriptionStoreUseCase(activeSubscription, symKey, { logger.log("Synced Subscriptions") }, { error -> logger.error(error) })

                        _events.emit(activeSubscription)
                        engineNotifySubscriptionNotifier.newlyRespondedRequestedSubscriptionId.updateAndGet { requestedSubscription.requestId to activeSubscription }
                    }, onFailure = {
                        logger.error(it)
                        return@supervisorScope _events.emit(SDKError(Resources.NotFoundException("Subscription already exists for topic: ${wcResponse.topic.value}")))
                    })
                }

                is JsonRpcResponse.JsonRpcError -> {
                    _events.emit(Error(wcResponse.response.id, response.error.message))
                }
            }
        } catch (exception: Exception) {
            _events.emit(SDKError(exception))
        }
    }
}