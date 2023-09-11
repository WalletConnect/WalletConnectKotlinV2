package com.walletconnect.notify.engine.domain

import android.net.Uri
import androidx.core.net.toUri
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.KeyStore
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.common.model.NotificationScope
import com.walletconnect.notify.common.model.ServerSubscription
import com.walletconnect.notify.common.model.Subscription
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.data.wellknown.config.NotifyConfigDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.net.URL

internal class SetActiveSubscriptionsUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val extractPublicKeysFromDidJsonUseCase: ExtractPublicKeysFromDidJsonUseCase,
    private val generateAppropriateUri: GenerateAppropriateUriUseCase,
    private val metadataRepository: MetadataStorageRepositoryInterface,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val serializer: JsonRpcSerializer,
    private val keyStore: KeyStore,
    private val logger: Logger,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(account: String, serverSubscriptions: List<ServerSubscription>): List<Subscription.Active> = supervisorScope {
        logger.log("SetActiveSubscriptionsUseCase - account: $account, serverSubscriptions(${serverSubscriptions.size}): $serverSubscriptions")

        val activeSubscriptions = serverSubscriptions.map { subscription ->
            with(subscription) {
                val dappUri = dappUrl.toUri()
                logger.log("SetActiveSubscriptionsUseCase - dappUri: $dappUri")

                val (metadata, scopes) = extractConfig(dappUri).getOrThrow()
                logger.log("SetActiveSubscriptionsUseCase - metadata: $metadata")
                logger.log("SetActiveSubscriptionsUseCase - scopes: $scopes")

                val (dappPublicKey, authenticationPublicKey) = extractPublicKeysFromDidJsonUseCase(dappUri).getOrThrow()
                logger.log("SetActiveSubscriptionsUseCase - dappPublicKey: $dappPublicKey")
                logger.log("SetActiveSubscriptionsUseCase - authenticationPublicKey: $authenticationPublicKey")

                val symmetricKey = SymmetricKey(symKey)
                val topic = Topic(sha256(symmetricKey.keyAsBytes))
                logger.log("SetActiveSubscriptionsUseCase - topic: $topic")

                metadataRepository.upsertPeerMetadata(topic, metadata, AppMetaDataType.PEER)

                keyStore.setKey(topic.value, symmetricKey)

                jsonRpcInteractor.subscribe(topic) { error -> launch { _events.emit(SDKError(error)); cancel() } }

                Subscription.Active(
                    AccountId(account),
                    scopes.associate { it.name to NotificationScope.Cached(it.name, it.description, true) },
                    Expiry(expiry),
                    authenticationPublicKey,
                    dappPublicKey,
                    topic,
                    metadata,
                    null,
                )
            }
        }

        subscriptionRepository.setActiveSubscriptions(account, activeSubscriptions)
        logger.log("SetActiveSubscriptionsUseCase - activeSubscriptions: $activeSubscriptions")

        return@supervisorScope activeSubscriptions
    }

    private suspend fun extractConfig(dappUri: Uri): Result<Pair<AppMetaData, List<NotificationScope.Remote>>> = withContext(Dispatchers.IO) {
        val notifyConfigDappUri = generateAppropriateUri(dappUri, WC_NOTIFY_CONFIG_JSON)

        return@withContext notifyConfigDappUri.runCatching {
            // Get the did.json from the dapp
            URL(this.toString()).openStream().bufferedReader().use { it.readText() }
        }.mapCatching { wellKnownNotifyConfigString ->
            // Parse the did.json
            serializer.tryDeserialize<NotifyConfigDTO>(wellKnownNotifyConfigString)
                ?: throw Exception("Failed to parse $WC_NOTIFY_CONFIG_JSON. Check that the $$WC_NOTIFY_CONFIG_JSON file matches the specs")
        }.mapCatching { notifyConfig ->
            Pair(
                notifyConfig.metaData,
                notifyConfig.types.map { typeDTO ->
                    NotificationScope.Remote(
                        name = typeDTO.name,
                        description = typeDTO.description
                    )
                }
            )
        }
    }


    private companion object {
        const val WC_NOTIFY_CONFIG_JSON = ".well-known/wc-notify-config.json"
    }
}