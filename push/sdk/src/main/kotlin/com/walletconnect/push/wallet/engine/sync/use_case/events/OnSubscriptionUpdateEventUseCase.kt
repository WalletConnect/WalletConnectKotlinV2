package com.walletconnect.push.wallet.engine.sync.use_case.events

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.android.sync.common.model.SyncUpdate
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.data.storage.SubscribeStorageRepository
import com.walletconnect.push.common.model.toDb
import com.walletconnect.push.wallet.engine.sync.model.SyncedSubscription
import com.walletconnect.push.wallet.engine.sync.model.toCommon

internal class OnSubscriptionUpdateEventUseCase(
    private val logger: Logger,
    private val keyManagementRepository: KeyManagementRepository,
    private val subscribeStorageRepository: SubscribeStorageRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    _moshi: Moshi.Builder,
) {
    private val moshi = _moshi.build()

    suspend operator fun invoke(event: Events.OnSyncUpdate) {

        if (event.update is SyncUpdate.SyncSet) {
            val update = (event.update as SyncUpdate.SyncSet)
            val syncedSubscription: SyncedSubscription = moshi.adapter(SyncedSubscription::class.java).fromJson(update.value) ?: return logger.error(event.toString())
            val activeSubscription = syncedSubscription.toCommon()

            keyManagementRepository.setKey(SymmetricKey(syncedSubscription.symKey), activeSubscription.pushTopic.value)

            runCatching {
                with(activeSubscription) {
                    subscribeStorageRepository.insertOrAbortActiveSubscription(
                        account.value,
                        expiry.seconds,
                        relay.protocol,
                        relay.data,
                        mapOfScope.toDb(),
                        dappGeneratedPublicKey.keyAsHex,
                        pushTopic.value,
                        responseTopic.value,
                    )
                }
            }.fold(
                onSuccess = { jsonRpcInteractor.subscribe(activeSubscription.pushTopic) { error -> logger.error(error) } },
                onFailure = { error -> logger.error("Failed to insert Synced Subscription: $error") }
            )


        } else {
            //todo: add for deletion
            //todo: write migration

        }
    }
}