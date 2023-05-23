package com.walletconnect.chat.engine.sync.use_case.requests

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.sync.client.Sync
import com.walletconnect.android.sync.client.SyncInterface
import com.walletconnect.android.sync.common.model.Store
import com.walletconnect.chat.engine.sync.ChatSyncStores
import com.walletconnect.chat.engine.sync.model.SyncedInviteKeys
import com.walletconnect.chat.engine.sync.model.toSync
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.Logger

internal class SetInviteKeyToChatInviteKeyStoreUseCase(
    private val logger: Logger,
    private val syncClient: SyncInterface,
    _moshi: Moshi.Builder,
) {
    private val moshi = _moshi.build()

    operator fun invoke(account: AccountId, invitePublicKey: PublicKey, invitePrivateKey: PrivateKey, onSuccess: (Boolean) -> Unit, onError: (Throwable) -> Unit) {
        val syncedInviteKeys = (invitePublicKey to invitePrivateKey).toSync(account)
        val payload = moshi.adapter(SyncedInviteKeys::class.java).toJson(syncedInviteKeys)
        logger.log("SyncedInviteKeys: $payload")


        syncClient.set(
            Sync.Params.Set(account, Store(ChatSyncStores.CHAT_INVITE_KEYS.value), account.value, payload),
            onSuccess = { didUpdate ->
                logger.log("Did update on ${ChatSyncStores.CHAT_INVITE_KEYS.value} happen: $didUpdate")
                onSuccess(didUpdate)
            },
            onError = { error ->
                logger.error(error.throwable)
                onError(error.throwable)
            }
        )
    }
}

