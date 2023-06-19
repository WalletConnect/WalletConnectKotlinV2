package com.walletconnect.android.sync.storage

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.sdk.storage.data.dao.sync.AccountsQueries
import com.walletconnect.android.sync.common.model.Account
import com.walletconnect.android.sync.common.model.Entropy

internal class AccountsStorageRepository(private val accounts: AccountsQueries) {

    suspend fun createAccount(account: Account) = with(account) {
        // Only insert when account does not yet exists in db. Entropy will be the same so no need for multiple inserts/updates
        accounts.insertOrAbortAccount(accountId = accountId.value, entropy = entropy.value)
    }

    suspend fun getAccount(accountId: AccountId): Account =
        accounts.getAccountByAccountId(accountId.value, ::dbToAccount).executeAsOne()

    private suspend fun doesAccountNotExists(accountId: AccountId) = accounts.doesAccountNotExists(accountId.value).executeAsOne()

    private fun dbToAccount(accountId: String, entropy: String): Account =
        Account(AccountId(accountId), Entropy(entropy))

}
