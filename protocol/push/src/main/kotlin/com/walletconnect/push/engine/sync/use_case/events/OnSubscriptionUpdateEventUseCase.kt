package com.walletconnect.push.engine.sync.use_case.events

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.android.sync.common.model.SyncUpdate
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.data.storage.SubscriptionRepository
import com.walletconnect.push.common.model.toDb
import com.walletconnect.push.data.MessagesRepository
import com.walletconnect.push.engine.sync.model.SyncedSubscription
import com.walletconnect.push.engine.sync.model.toCommon
import timber.log.Timber

internal class OnSubscriptionUpdateEventUseCase(
    private val logger: Logger,
    private val keyManagementRepository: KeyManagementRepository,
    private val messagesRepository: MessagesRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    _moshi: Moshi.Builder,
) {
    private val moshi = _moshi.build()

    suspend operator fun invoke(event: Events.OnSyncUpdate) {

        when (val update = event.update) {
            is SyncUpdate.SyncSet -> {
                val syncedSubscription: SyncedSubscription = moshi.adapter(SyncedSubscription::class.java).fromJson(update.value) ?: return logger.error(event.toString())
                val activeSubscription = syncedSubscription.toCommon()

                keyManagementRepository.setKey(SymmetricKey(syncedSubscription.symKey), activeSubscription.pushTopic.value)

                with(activeSubscription) {
                    runCatching {
                        subscriptionRepository.insertOrAbortActiveSubscription(
                            account.value,
                            expiry.seconds,
                            relay.protocol,
                            relay.data,
                            mapOfScope.toDb(),
                            dappGeneratedPublicKey.keyAsHex,
                            pushTopic.value,
                            null,
                        )
                    }.mapCatching {
                        metadataStorageRepository.insertOrAbortMetadata(topic = pushTopic, appMetaData = dappMetaData!!, appMetaDataType = AppMetaDataType.PEER)
                    }.fold(
                        onFailure = { error -> logger.error("Failed to insert Synced Subscription: $error") },
                        onSuccess = {
                            jsonRpcInteractor.subscribe(pushTopic) { error -> logger.error(error) }
                            getPushMessagesFromHistory(pushTopic) { messagesCount -> logger.log("Fetched $messagesCount messages from ${dappMetaData!!.url}") }
                        }
                    )
                }
            }

            is SyncUpdate.SyncDelete -> {
                val pushTopic = update.key
                subscriptionRepository.deleteSubscriptionByPushTopic(pushTopic)
                messagesRepository.deleteMessagesByTopic(pushTopic)
                jsonRpcInteractor.unsubscribe(Topic(pushTopic))
                Timber.d("Sync deleting: $pushTopic")
            }
        }
    }

    private suspend fun getPushMessagesFromHistory(pushTopic: Topic, onSuccess: (Int) -> Unit) {
    }
}