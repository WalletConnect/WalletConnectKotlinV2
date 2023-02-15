package com.walletconnect.chat.storage

import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.storage.data.dao.IdentitiesQueries

internal class IdentitiesStorageRepository(private val identities: IdentitiesQueries) {
    suspend fun insertIdentity(identityPublicKey: String, accountId: AccountId) = identities.insertOrAbortIdentity(identityPublicKey, accountId.value)
    suspend fun getAccountId(identityPublicKey: String) = AccountId(identities.getAccountIdByIdentity(identityPublicKey).executeAsOne())
}
