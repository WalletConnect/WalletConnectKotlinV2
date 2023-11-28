package com.walletconnect.notify.engine.domain

import androidx.core.net.toUri
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.key_chain.KeyStore
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.jwt.decodeEd25519DidKey
import com.walletconnect.notify.common.model.NotificationScope
import com.walletconnect.notify.common.model.ServerSubscription
import com.walletconnect.notify.common.model.Subscription
import com.walletconnect.notify.data.storage.SubscriptionRepository
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class SetActiveSubscriptionsUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val extractMetadataFromConfigUseCase: ExtractMetadataFromConfigUseCase,
    private val metadataRepository: MetadataStorageRepositoryInterface,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val keyStore: KeyStore,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(account: String, serverSubscriptions: List<ServerSubscription>): List<Subscription.Active> = supervisorScope {
        val activeSubscriptions = serverSubscriptions.map { subscription ->
            with(subscription) {
                val dappUri = appDomainWithHttps.toUri()

                //TODO: Those errors are not caught and handled
                val (metadata, scopes) = extractMetadataFromConfigUseCase(dappUri).getOrThrow()
                val selectedScopes = scopes.associate { remote ->
                    remote.id to NotificationScope.Cached(
                        name = remote.name, description = remote.description, id = remote.id,
                        isSelected = subscription.scope.firstOrNull { serverScope -> serverScope == remote.id } != null
                    )
                }

                val symmetricKey = SymmetricKey(symKey)
                val topic = Topic(sha256(symmetricKey.keyAsBytes))

                metadataRepository.upsertPeerMetadata(topic, metadata, AppMetaDataType.PEER)

                keyStore.setKey(topic.value, symmetricKey)

                jsonRpcInteractor.subscribe(topic) { error -> launch { _events.emit(SDKError(error)); cancel() } }

                Subscription.Active(AccountId(account), selectedScopes, Expiry(expiry), decodeEd25519DidKey(appAuthenticationKey), topic, metadata, null)
            }
        }

        subscriptionRepository.setActiveSubscriptions(account, activeSubscriptions)

        return@supervisorScope activeSubscriptions
    }
}