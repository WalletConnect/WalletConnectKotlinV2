package com.walletconnect.android.sync.engine.use_case.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.sync.common.exception.validateAccountId

internal object GetMessageUseCase : GetMessageUseCaseInterface {
    private const val SYNC_MESSAGE_PREFIX = "I authorize this app to sync my account: "
    private const val SYNC_MESSAGE_SUFFIX = "\n\nRead more about it here: https://walletconnect.com/faq"

    override fun getMessage(accountId: AccountId): String {
        validateAccountId(accountId) { error -> throw error }

        return accountId.toSyncMessage()
    }

    private fun AccountId.toSyncMessage() = SYNC_MESSAGE_PREFIX + this.value + SYNC_MESSAGE_SUFFIX
}

internal interface GetMessageUseCaseInterface {
    fun getMessage(accountId: AccountId): String
}


