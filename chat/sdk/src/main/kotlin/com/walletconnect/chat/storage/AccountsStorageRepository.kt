package com.walletconnect.chat.storage

import com.walletconnect.chat.common.model.Account
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.chat.storage.data.dao.AccountsQueries
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

internal class AccountsStorageRepository(private val accounts: AccountsQueries) {

    private suspend fun doesAccountNotExists(accountId: AccountId) = accounts.doesAccountNotExists(accountId.value).executeAsOne()

    suspend fun upsertAccount(account: Account) =
        if (doesAccountNotExists(account.accountId)) {
            createAccount(account)
        } else {
            updateAccount(account)
        }

    private suspend fun updateAccount(account: Account) = with(account) {
        accounts.updateAccount(publicIdentityKey.keyAsHex, publicInviteKey?.keyAsHex, inviteTopic?.value, accountId.value)
    }

    private suspend fun createAccount(account: Account) = with(account) {
        accounts.insertOrAbortAccount(accountId = accountId.value, publicIdentityKey = publicIdentityKey.keyAsHex, publicInviteKey = publicInviteKey?.keyAsHex, inviteTopic = inviteTopic?.value)
    }

    suspend fun getAllInviteTopics(): List<Topic> =
        accounts.getAllInviteTopics().executeAsList().map { dbToTopic(it) }

    suspend fun getAccountByAccountId(accountId: AccountId): Account =
        accounts.getAccountByAccountId(accountId.value, ::dbToAccount).executeAsOne()

    suspend fun getAccountByInviteTopic(inviteTopic: Topic): Account =
        accounts.getAccountByInviteTopic(inviteTopic.value, ::dbToAccount).executeAsOne()

    suspend fun deleteAccountByAccountId(accountId: AccountId) =
        accounts.deleteAccountByAccountId(accountId.value)

    suspend fun setAccountPublicInviteKey(accountId: AccountId, publicInviteKey: PublicKey?, inviteTopic: Topic?) =
        accounts.updateAccountPublicInviteKey(publicInviteKey?.keyAsHex, inviteTopic?.value, accountId.value)

    suspend fun removeAccountPublicInviteKey(accountId: AccountId) =
        accounts.removeAccountPublicInviteKey(accountId.value)

    private fun dbToTopic(inviteTopic: String) = Topic(inviteTopic)

    private fun dbToAccount(accountId: String, publicIdentityKey: String, publicInviteKey: String?, inviteTopic: String?): Account =
        Account(AccountId(accountId), PublicKey(publicIdentityKey), publicInviteKey?.let { PublicKey(publicInviteKey) }, inviteTopic?.let { Topic(inviteTopic) })
}