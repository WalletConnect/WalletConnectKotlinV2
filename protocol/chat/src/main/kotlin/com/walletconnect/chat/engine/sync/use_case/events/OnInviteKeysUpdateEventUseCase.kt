package com.walletconnect.chat.engine.sync.use_case.events

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.getInviteTag
import com.walletconnect.android.internal.utils.getParticipantTag
import com.walletconnect.android.sync.common.model.Events
import com.walletconnect.android.sync.common.model.SyncUpdate
import com.walletconnect.chat.engine.sync.model.SyncedInviteKeys
import com.walletconnect.chat.engine.sync.model.toCommon
import com.walletconnect.chat.storage.AccountsStorageRepository
import com.walletconnect.foundation.util.Logger

internal class OnInviteKeysUpdateEventUseCase(
    private val logger: Logger,
    private val accountsRepository: AccountsStorageRepository,
    private val keyManagementRepository: KeyManagementRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    _moshi: Moshi.Builder,
) {
    private val moshi = _moshi.build()

    suspend operator fun invoke(event: Events.OnSyncUpdate) {
        val accountId = AccountId(event.update.key)
        val storedAccount = runCatching { accountsRepository.getAccountByAccountId(accountId) }.getOrNull()

        when (val update = event.update) {
            is SyncUpdate.SyncSet -> {
                val syncedInviteKeys: SyncedInviteKeys = moshi.adapter(SyncedInviteKeys::class.java).fromJson(update.value) ?: return logger.error(event.toString())
                val (invitePublicKey, invitePrivateKey) = syncedInviteKeys.toCommon()
                val inviteTopic = keyManagementRepository.getTopicFromKey(invitePublicKey)

                if (storedAccount == null) {
                    logger.error(Throwable("Account: $accountId was not registered"))
                } else {
                    keyManagementRepository.setKeyPair(invitePublicKey, invitePrivateKey)
                    keyManagementRepository.setKey(invitePublicKey, accountId.getInviteTag())
                    keyManagementRepository.setKey(invitePublicKey, inviteTopic.getParticipantTag())
                    accountsRepository.setAccountPublicInviteKey(accountId, invitePublicKey, inviteTopic)
                    jsonRpcInteractor.subscribe(inviteTopic)
                }
            }
            is SyncUpdate.SyncDelete -> {
                if (storedAccount == null) {
                    logger.error(Throwable("Account: $accountId was not registered"))
                } else {
                    if (storedAccount.publicInviteKey == null) {
                        logger.error(Throwable("Account: $accountId has no invite key stored"))
                    } else {
                        accountsRepository.removeAccountPublicInviteKey(accountId)
                        keyManagementRepository.removeKeys(accountId.getInviteTag())
                        val inviteTopic = keyManagementRepository.getTopicFromKey(storedAccount.publicInviteKey)
                        keyManagementRepository.removeKeys(inviteTopic.getParticipantTag())
                        jsonRpcInteractor.unsubscribe(inviteTopic)
                    }
                }
            }
        }
    }
}