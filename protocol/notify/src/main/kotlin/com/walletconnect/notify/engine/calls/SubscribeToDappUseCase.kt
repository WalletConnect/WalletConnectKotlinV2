@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import android.net.Uri
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.monthInSeconds
import com.walletconnect.android.internal.utils.thirtySeconds
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.notify.common.model.CreateSubscription
import com.walletconnect.notify.common.model.Scope
import com.walletconnect.notify.common.model.NotifyRpc
import com.walletconnect.notify.common.model.Subscription
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.engine.BLOCKING_CALLS_DELAY_INTERVAL
import com.walletconnect.notify.engine.BLOCKING_CALLS_TIMEOUT
import com.walletconnect.notify.engine.domain.ExtractMetadataFromConfigUseCase
import com.walletconnect.notify.engine.domain.ExtractPublicKeysFromDidJsonUseCase
import com.walletconnect.notify.engine.domain.FetchDidJwtInteractor
import com.walletconnect.notify.engine.responses.OnSubscribeResponseUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout

typealias DidJsonPublicKeyPair = Pair<PublicKey, PublicKey>

internal class SubscribeToDappUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val extractMetadataFromConfigUseCase: ExtractMetadataFromConfigUseCase,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val fetchDidJwtInteractor: FetchDidJwtInteractor,
    private val extractPublicKeysFromDidJson: ExtractPublicKeysFromDidJsonUseCase,
    private val onSubscribeResponseUseCase: OnSubscribeResponseUseCase,
    private val subscriptionRepository: SubscriptionRepository,
) : SubscribeToDappUseCaseInterface {

    //todo: verify if i can call it multiple times
    override suspend fun subscribeToDapp(dappUri: Uri, account: String): CreateSubscription = supervisorScope {
        try {
            val result = MutableStateFlow<CreateSubscription>(CreateSubscription.Processing)

            val (dappPublicKey, authenticationPublicKey) = extractPublicKeysFromDidJson(dappUri).getOrThrow()
            val (dappMetaData, dappScopes) = extractMetadataFromConfigUseCase(dappUri).getOrThrow()

            val subscribeTopic = Topic(sha256(dappPublicKey.keyAsBytes))
            val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
            val responseTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, dappPublicKey)

            metadataStorageRepository.insertOrAbortMetadata(topic = responseTopic, appMetaData = dappMetaData, appMetaDataType = AppMetaDataType.PEER)

            val didJwt = fetchDidJwtInteractor.subscriptionRequest(AccountId(account), authenticationPublicKey, dappUri.toString(), dappScopes.map { it.id }).getOrThrow()

            val params = CoreNotifyParams.SubscribeParams(didJwt.value)
            val request = NotifyRpc.NotifySubscribe(params = params)
            val irnParams = IrnParams(Tags.NOTIFY_SUBSCRIBE, Ttl(thirtySeconds))

            onSubscribeResponseUseCase.events
                .filter { it.first == params }
                .map { it.second }
                .filter { it is CreateSubscription.Success || it is CreateSubscription.Error }
                .onEach { result.emit(it as CreateSubscription) }
                .launchIn(scope)

            val selectedScopes = dappScopes
                .associate { remote -> remote.id to Scope.Cached(name = remote.name, description = remote.description, id = remote.id, isSelected = true) }

            // optimistically add active subscription which will be updated on response with the correct expiry
            // necessary for welcoming messages that could be sent before the subscription response is received
            val activeSubscription = Subscription.Active(AccountId(account), selectedScopes, Expiry(monthInSeconds), authenticationPublicKey, responseTopic, dappMetaData, null)
            subscriptionRepository.insertOrAbortSubscription(account, activeSubscription)
            jsonRpcInteractor.subscribe(responseTopic)

            jsonRpcInteractor.publishJsonRpcRequest(
                topic = subscribeTopic,
                params = irnParams,
                payload = request,
                envelopeType = EnvelopeType.ONE,
                participants = Participants(selfPublicKey, dappPublicKey),
                onFailure = { error -> result.value = CreateSubscription.Error(error) },
            )

            withTimeout(BLOCKING_CALLS_TIMEOUT) {
                while (result.value == CreateSubscription.Processing) {
                    delay(BLOCKING_CALLS_DELAY_INTERVAL)
                }
            }

            return@supervisorScope result.value
        } catch (e: Exception) {
            return@supervisorScope CreateSubscription.Error(e)
        }
    }
}

internal interface SubscribeToDappUseCaseInterface {
    suspend fun subscribeToDapp(dappUri: Uri, account: String): CreateSubscription
}