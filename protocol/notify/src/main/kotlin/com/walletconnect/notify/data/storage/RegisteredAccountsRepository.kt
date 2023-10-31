@file:JvmSynthetic

package com.walletconnect.notify.data.storage

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.notify.common.model.RegisteredAccount
import com.walletconnect.notify.common.storage.data.dao.RegisteredAccountsQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class RegisteredAccountsRepository(private val registeredAccounts: RegisteredAccountsQueries) {

    suspend fun insertOrIgnoreAccount(accountId: AccountId, publicIdentityKey: PublicKey, isLimited: Boolean, appDomain: String?) = withContext(Dispatchers.IO) {
        registeredAccounts.insertOrIgnoreAccount(accountId.value, publicIdentityKey.keyAsHex, isLimited, appDomain)
    }

    suspend fun updateNotifyServerData(accountId: AccountId, notifyServerWatchTopic: Topic, notifyServerAuthenticationKey: PublicKey) = withContext(Dispatchers.IO) {
        registeredAccounts.updateNotifyServerData(
            accountId = accountId.value, notifyServerWatchTopic = notifyServerWatchTopic.value, notifyServerAuthenticationKey = notifyServerAuthenticationKey.keyAsHex
        )
    }

    suspend fun getAccountByAccountId(accountId: String): RegisteredAccount = withContext(Dispatchers.IO) {
        registeredAccounts.getAccountByAccountId(accountId, ::toRegisterAccount).executeAsOne()
    }

    suspend fun getAccountByIdentityKey(identityPublicKey: String): RegisteredAccount = withContext(Dispatchers.IO) {
        registeredAccounts.getAccountByIdentityKey(identityPublicKey, ::toRegisterAccount).executeAsOne()
    }

    suspend fun getAllAccounts(): List<RegisteredAccount> = withContext(Dispatchers.IO) {
        registeredAccounts.getAllAccounts(::toRegisterAccount).executeAsList()
    }

    suspend fun deleteAccountByAccountId(accountId: String) = withContext(Dispatchers.IO) {
        registeredAccounts.deleteAccountByAccountId(accountId)
    }

    private fun toRegisterAccount(
        accountId: String, publicIdentityKey: String, isLimited: Boolean, appDomain: String?, notifyServerWatchTopic: String?, notifyServerAuthenticationKey: String?,
    ): RegisteredAccount =
        RegisteredAccount(AccountId(accountId), PublicKey(publicIdentityKey), isLimited, appDomain, notifyServerWatchTopic?.let { Topic(it) }, notifyServerAuthenticationKey?.let { PublicKey(it) })
}