package com.walletconnect.chat.engine.sync.use_case

import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.sync.client.Sync
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.chat.common.exceptions.ChatSyncStoresInitializationTimeoutException
import com.walletconnect.chat.engine.sync.ChatSyncStores
import com.walletconnect.foundation.util.Logger
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class SetupSyncInChatUseCase(
    private val syncClient: SyncInterface,
    private val logger: Logger,
) {
    operator fun invoke(accountId: AccountId, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        syncClient.isRegistered(Sync.Params.IsRegistered(accountId), onError = { onError(Throwable("Failed to check account is registered")) }, onSuccess = { isRegistered ->
            // Lambda that register required chat stores within sync client
            val registerChatStoresInSync = { registerChatStoresInSync(accountId, onSuccess, onError) }

            if (!isRegistered) {
                registerAccountInSync(accountId, onSign, onAccountRegisterSuccess = registerChatStoresInSync, onError)
            } else {
                registerChatStoresInSync()
            }
        })
    }

    /**
     * Registers account in Sync Client and calls [onAccountRegisterSuccess] on success
     */
    private fun registerAccountInSync(accountId: AccountId, onSign: (String) -> Cacao.Signature?, onAccountRegisterSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        val syncSignature = onSign(syncClient.getMessage(Sync.Params.GetMessage(accountId))) ?: return onError(Throwable("Signing Sync SDK message is required to use Chat SDK"))

        val params = Sync.Params.Register(accountId, Sync.Model.Signature(syncSignature.t, syncSignature.s, syncSignature.m), SignatureType.headerOf(syncSignature.t))

        syncClient.register(params, onSuccess = { onAccountRegisterSuccess() }, onError = { error -> onError(error.throwable) })
    }


    /**
     * Creates required Chat stores in Sync Client and calls [onStoreRegisterSuccess] on success
     */
    private fun registerChatStoresInSync(accountId: AccountId, onStoreRegisterSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        // Register blocking current thread all stores necessary to sync chat state
        val countDownLatch = CountDownLatch(ChatSyncStores.values().size)

        // Note: When I tried registering all stores simultaneously I had issues with getting right values, when doing it sequentially it works
        ChatSyncStores.values().forEach { store ->
            syncClient.create(Sync.Params.Create(accountId, Store(store.value)),
                onSuccess = { countDownLatch.countDown() },
                onError = { error -> logger.error("Error while registering ${store.value}: ${error.throwable.stackTraceToString()}") }
            )
        }

        if (!countDownLatch.await(5, TimeUnit.SECONDS)) {
            onError(ChatSyncStoresInitializationTimeoutException)
        } else {
            onStoreRegisterSuccess()
        }
    }
}