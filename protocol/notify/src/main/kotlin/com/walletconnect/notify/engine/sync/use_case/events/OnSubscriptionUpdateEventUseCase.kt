@file:JvmSynthetic

package com.walletconnect.notify.engine.sync.use_case.events

import com.squareup.moshi.Moshi
import com.walletconnect.android.history.HistoryInterface
import com.walletconnect.android.history.network.model.messages.MessagesParams
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.android.sync.common.model.SyncUpdate
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.common.model.toDb
import com.walletconnect.notify.data.storage.MessagesRepository
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.engine.sync.model.SyncedSubscription
import com.walletconnect.notify.engine.sync.model.toCommon
import timber.log.Timber

internal class OnSubscriptionUpdateEventUseCase(
    private val logger: Logger,
    private val keyManagementRepository: KeyManagementRepository,
    private val messagesRepository: MessagesRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val historyInterface: HistoryInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    @Suppress("LocalVariableName") _moshi: Moshi.Builder,
) {
    private val moshi = _moshi.build()

    suspend operator fun invoke(event: Events.OnSyncUpdate) {

        when (val update = event.update) {
            is SyncUpdate.SyncSet -> {
                val syncedSubscription: SyncedSubscription = moshi.adapter(SyncedSubscription::class.java).fromJson(update.value) ?: return logger.error(event.toString())
                val activeSubscription = syncedSubscription.toCommon()

                keyManagementRepository.setKey(SymmetricKey(syncedSubscription.symKey), activeSubscription.notifyTopic.value)

                with(activeSubscription) {
                    runCatching<Unit> {
                        subscriptionRepository.insertOrAbortActiveSubscription(
                            account.value,
                            expiry.seconds,
                            relay.protocol,
                            relay.data,
                            mapOfNotificationScope.toDb(),
                            dappGeneratedPublicKey.keyAsHex,
                            notifyTopic.value,
                            null,
                        )
                    }.mapCatching {
                        metadataStorageRepository.insertOrAbortMetadata(topic = notifyTopic, appMetaData = dappMetaData!!, appMetaDataType = AppMetaDataType.PEER)
                    }.fold(
                        onFailure = { error -> logger.error("Failed to insert Synced Subscription: $error") },
                        onSuccess = {
                            jsonRpcInteractor.subscribe(notifyTopic) { error -> logger.error(error) }
                            getNotifyMessagesFromHistory(notifyTopic) { messagesCount ->
                                if (messagesCount >= messagesBatchSize) logger.error("Fetched $messagesBatchSize for ${dappMetaData!!.url}")
                                else logger.log("Fetched $messagesCount for ${dappMetaData!!.url}")

                                Timber.d("Sync getNotifyMessagesFromHistory: $notifyTopic")
                            }
                        }
                    )
                }
            }

            is SyncUpdate.SyncDelete -> {
                val notifyTopic = update.key
                subscriptionRepository.deleteSubscriptionByNotifyTopic(notifyTopic)
                messagesRepository.deleteMessagesByTopic(notifyTopic)
                jsonRpcInteractor.unsubscribe(Topic(notifyTopic))
                Timber.d("Sync deleting: $notifyTopic")
            }
        }
    }

    private suspend fun getNotifyMessagesFromHistory(notifyTopic: Topic, onSuccess: (Int) -> Unit) {
        historyInterface.getMessages(
            MessagesParams(notifyTopic.value, null, messagesBatchSize, null),
            onError = { error -> logger.error(error.throwable) },
            onSuccess = { onSuccess(it.size) }
        )
    }

    private companion object {
        const val messagesBatchSize = 200L
    }
}