@file:JvmSynthetic

package com.walletconnect.notify.engine.sync.use_case.events

import androidx.core.net.toUri
import com.squareup.moshi.Moshi
import com.walletconnect.android.archive.ArchiveInterface
import com.walletconnect.android.archive.network.model.messages.MessagesParams
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
import com.walletconnect.notify.engine.domain.ExtractPublicKeysFromDidJsonUseCase
import com.walletconnect.notify.engine.sync.model.SyncedSubscription
import com.walletconnect.notify.engine.sync.model.toCommon
import timber.log.Timber

internal class OnSubscriptionUpdateEventUseCase(
    private val logger: Logger,
    private val keyManagementRepository: KeyManagementRepository,
    private val messagesRepository: MessagesRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val archiveInterface: ArchiveInterface,
    private val subscriptionRepository: SubscriptionRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val extractPublicKeysFromDidJsonUseCase: ExtractPublicKeysFromDidJsonUseCase,
    @Suppress("LocalVariableName") _moshi: Moshi.Builder,
) {
    private val moshi = _moshi.build()

    suspend operator fun invoke(event: Events.OnSyncUpdate) {
        when (val update = event.update) {
            is SyncUpdate.SyncSet -> {
                runCatching { moshi.adapter(SyncedSubscription::class.java).fromJson(update.value)!! }
                    .onFailure { error -> logger.error("Failed to parse Synced Subscription: ${error.stackTraceToString()}") }
                    .mapCatching { syncedSubscription: SyncedSubscription ->
                        val dappUri = syncedSubscription.metadata.url.toUri().run {
                            "$scheme://$authority".toUri()
                        }
                        val (_, authenticationPublicKey) = extractPublicKeysFromDidJsonUseCase(dappUri).getOrThrow()

                        syncedSubscription.toCommon(authenticationPublicKey).also { activeSubscription ->
                            keyManagementRepository.setKey(SymmetricKey(syncedSubscription.symKey), activeSubscription.notifyTopic.value)
                        }
                    }.onSuccess { activeSubscription ->
                        with(activeSubscription) {
                            subscriptionRepository.upsertOrAbortActiveSubscription(
                                account.value,
                                authenticationPublicKey,
                                expiry.seconds,
                                relay.protocol,
                                relay.data,
                                mapOfNotificationScope.toDb(),
                                dappGeneratedPublicKey.keyAsHex,
                                notifyTopic.value,
                                null, // This is not synced with an active subscription
                            )
                        }
                    }.onSuccess { activeSubscription ->
                        with(activeSubscription) {
                            metadataStorageRepository.upsertPeerMetadata(topic = notifyTopic, appMetaData = dappMetaData!!, appMetaDataType = AppMetaDataType.PEER)
                        }
                    }.fold(
                        onSuccess = { activeSubscription ->
                            jsonRpcInteractor.subscribe(activeSubscription.notifyTopic) { error -> logger.error(error) }
                            getNotifyMessagesFromHistory(activeSubscription.notifyTopic) { messagesCount -> logger.log("Fetched $messagesCount messages from ${activeSubscription.dappMetaData!!.url}") }
                        },
                        onFailure = { error ->
                            logger.error("Failed to insert Synced Subscription: $error")
                        }
                    )
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
        archiveInterface.getAllMessages(
            MessagesParams(notifyTopic.value, null, ArchiveInterface.DEFAULT_BATCH_SIZE, null),
            onError = { error -> logger.error(error.throwable) },
            onSuccess = { onSuccess(it.size) }
        )
    }
}