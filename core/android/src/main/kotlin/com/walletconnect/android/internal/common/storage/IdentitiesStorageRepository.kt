package com.walletconnect.android.internal.common.storage

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.sdk.storage.data.dao.IdentitiesQueries

class IdentitiesStorageRepository(private val identities: IdentitiesQueries) {
    suspend fun insertIdentity(identityPublicKey: String, accountId: AccountId) = identities.insertOrAbortIdentity(identityPublicKey, accountId.value)
    suspend fun getAccountId(identityPublicKey: String) = AccountId(identities.getAccountIdByIdentity(identityPublicKey).executeAsOne())
}
