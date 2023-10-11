package com.walletconnect.android.internal.common.storage

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.sdk.storage.data.dao.IdentitiesQueries

class IdentitiesStorageRepository(private val identities: IdentitiesQueries, moshiBuilder: Moshi.Builder) {
    private val moshi = moshiBuilder.build()

    suspend fun insertIdentity(identityPublicKey: String, accountId: AccountId, cacaoPayload: Cacao.Payload, isMine: Boolean) =
        identities.insertOrAbortIdentity(identityPublicKey, accountId.value, moshi.adapter(Cacao.Payload::class.java).toJson(cacaoPayload), isMine)

    suspend fun removeIdentity(identityPublicKey: String) = identities.removeIdentity(identityPublicKey)

    suspend fun getAccountId(identityPublicKey: String) = AccountId(identities.getAccountIdByIdentity(identityPublicKey).executeAsOne())

    suspend fun getCacaoPayloadByIdentity(identityPublicKey: String): Cacao.Payload? =
        runCatching { identities.getCacaoPayloadByIdentity(identityPublicKey).executeAsOne().cacao_payload?.let { moshi.adapter(Cacao.Payload::class.java).fromJson(it) } }.getOrNull()
}
