package com.walletconnect.chat.engine.sync.model

import com.squareup.moshi.JsonClass
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey

@JsonClass(generateAdapter = true)
internal data class SyncedInviteKeys(
    val publicKey: String,
    val privateKey: String,
)

internal fun Pair<PublicKey, PrivateKey>.toSync() =
    SyncedInviteKeys(publicKey = first.keyAsHex, privateKey = second.keyAsHex)

internal fun SyncedInviteKeys.toCommon(): Pair<PublicKey, PrivateKey> = PublicKey(publicKey) to PrivateKey(privateKey)