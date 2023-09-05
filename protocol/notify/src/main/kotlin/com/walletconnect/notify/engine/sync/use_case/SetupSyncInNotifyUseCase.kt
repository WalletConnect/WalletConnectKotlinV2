@file:JvmSynthetic

package com.walletconnect.notify.engine.sync.use_case

import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.sync.client.Sync
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.foundation.util.Logger
import com.walletconnect.notify.engine.sync.NotifySyncStores
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class SetupSyncInNotifyUseCase(
    private val syncClient: SyncInterface,
    private val logger: Logger,
) {
    operator fun invoke(accountId: AccountId, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        syncClient.isRegistered(Sync.Params.IsRegistered(accountId), onError = { onError(Throwable("Failed to check account is registered")) }, onSuccess = { isRegistered ->
            // Lambda that register required notify stores within sync client
            val registerNotifyStoresInSync = { registerNotifyStoresInSync(accountId, onSuccess, onError) }

            if (!isRegistered) {
                // If account is not registered then register in sync client and later register required notify stores
                registerAccountInSync(accountId, onSign, onAccountRegisterSuccess = registerNotifyStoresInSync, onError)
            } else {
                // If account is registered then only register required notify stores
                registerNotifyStoresInSync()
            }
        })
    }

    /**
     * Registers account in Sync Client and calls [onAccountRegisterSuccess] on success
     */
    private fun registerAccountInSync(accountId: AccountId, onSign: (String) -> Cacao.Signature?, onAccountRegisterSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        val syncSignature = onSign(syncClient.getMessage(Sync.Params.GetMessage(accountId))) ?: return onError(Throwable("Signing Sync SDK message is required to use Notify SDK"))

        val params = Sync.Params.Register(accountId, Sync.Model.Signature(syncSignature.t, syncSignature.s, syncSignature.m), SignatureType.headerOf(syncSignature.t))

        syncClient.register(params, onSuccess = { onAccountRegisterSuccess() }, onError = { error -> onError(error.throwable) })
    }


    /**
     * Creates required Notify stores in Sync Client and calls [onStoreRegisterSuccess] on success
     */
    private fun registerNotifyStoresInSync(accountId: AccountId, onStoreRegisterSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        // Register blocking current thread all stores necessary to sync notify state
        val countDownLatch = CountDownLatch(NotifySyncStores.values().size)

        // Note: When I tried registering all stores simultaneously I had issues with getting right values, when doing it sequentially it works
        NotifySyncStores.values().forEach { store ->
            syncClient.create(
                Sync.Params.Create(accountId, Store(store.value)),
                onSuccess = { countDownLatch.countDown() },
                onError = { error -> logger.error("Error while registering ${store.value}: ${error.throwable.stackTraceToString()}") }
            )
        }

        if (!countDownLatch.await(5, TimeUnit.SECONDS)) {
            onError(Throwable("Required Notify Stores initialization timeout"))
        } else {
            onStoreRegisterSuccess()
        }
    }
}