package com.walletconnect.sync.engine.use_case

import com.walletconnect.android.internal.common.model.AccountId

internal object GetMessageUseCase : GetMessageUseCaseInterface {
    private const val SYNC_MESSAGE_PREFIX = "I authorize this app to sync my account: "
    private const val SYNC_MESSAGE_SUFFIX = "\n\nRead more about Sync API: https://docs.walletconnect.com/2.0/specs/clients/sync"

    override fun getMessage(accountId: AccountId): String {
        return accountId.toSyncMessage()
    }

    private fun AccountId.toSyncMessage() = SYNC_MESSAGE_PREFIX + this.value + SYNC_MESSAGE_SUFFIX
}

internal interface GetMessageUseCaseInterface {
    fun getMessage(accountId: AccountId): String
}


