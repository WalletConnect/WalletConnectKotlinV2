package com.walletconnect.chat.engine.sync.model

import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.chat.common.model.Account
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey

@JsonClass(generateAdapter = true)
internal data class SyncedInviteKeys(
    val publicKey: String,
    val privateKey: String,
    val account: String,
)

internal fun Pair<PublicKey, PrivateKey>.toSync(account: AccountId) =
    SyncedInviteKeys(publicKey = first.keyAsHex, privateKey = second.keyAsHex, account.value)

internal fun SyncedInviteKeys.toCommon(): Pair<PublicKey, PrivateKey> = PublicKey(publicKey) to PrivateKey(privateKey)