@file:JvmSynthetic

package com.walletconnect.notify.data.storage

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.notify.common.model.RegisteredAccount
import com.walletconnect.notify.common.storage.data.dao.RegisteredAccountsQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class RegisteredAccountsRepository(private val registeredAccounts: RegisteredAccountsQueries) {

    suspend fun insertOrIgnoreAccount(
        accountId: AccountId,
        publicIdentityKey: PublicKey,
        isLimited: Boolean,
        appDomain: String?
    ) = withContext(Dispatchers.IO) {
        registeredAccounts.insertOrIgnoreAccount(accountId.value, publicIdentityKey.keyAsHex, isLimited, appDomain)
    }

    suspend fun getAccountByAccountId(accountId: String): RegisteredAccount = withContext(Dispatchers.IO) {
        registeredAccounts.getAccountByAccountId(accountId, ::toRegisterAccount).executeAsOne()
    }

    suspend fun getAllAccounts(): List<RegisteredAccount> = withContext(Dispatchers.IO) {
        registeredAccounts.getAllAccounts(::toRegisterAccount).executeAsList()
    }

    suspend fun deleteAccountByAccountId(accountId: String) = withContext(Dispatchers.IO) {
        registeredAccounts.deleteAccountByAccountId(accountId)
    }

    private fun toRegisterAccount(
        accountId: String,
        publicIdentityKey: String,
        isLimited: Boolean,
        appDomain: String?
    ): RegisteredAccount = RegisteredAccount(AccountId(accountId), PublicKey(publicIdentityKey), isLimited, appDomain)
}