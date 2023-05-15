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
    private val syncInterface: SyncInterface,
    private val logger: Logger,
) {
    operator fun invoke(accountId: AccountId, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        // todo check if someone already register before, possible only after https://github.com/WalletConnect/walletconnect-docs/pull/680 is merged
        val syncSignature = onSign(syncInterface.getMessage(Sync.Params.GetMessage(accountId))) ?: return onError(Throwable("Signing Sync SDK message is required to use Chat SDK"))

        val params = Sync.Params.Register(accountId, Sync.Model.Signature(syncSignature.t, syncSignature.s, syncSignature.m), SignatureType.headerOf(syncSignature.t))

        syncInterface.register(params, onSuccess = {

            // Register blocking current thread all stores necessary to sync chat state
            val countDownLatch = CountDownLatch(ChatSyncStores.values().size)

            // Note: When I tried registering all stores simultaneously I had issues with getting right values, when doing it sequentially it works
            ChatSyncStores.values().forEach { store ->
                logger.log("Registering store: $store")
                syncInterface.create(Sync.Params.Create(accountId, Store(store.value)), onSuccess = { countDownLatch.countDown() }, onError = {})
            }

            if (!countDownLatch.await(5, TimeUnit.SECONDS)) {
                onError(ChatSyncStoresInitializationTimeoutException)
            } else {
                onSuccess()
            }
        }, onError = { error -> onError(error.throwable) })
    }
}