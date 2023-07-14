package com.walletconnect.chat.engine.sync.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.chat.common.model.Thread
import com.walletconnect.foundation.common.model.Topic

@JsonClass(generateAdapter = true)
internal data class SyncedThread(
    val topic: String,
    val selfAccount: String,
    val peerAccount: String,
    @Json(name = "symKey") val symmetricKey: String,
)

internal fun Thread.toSync(symmetricKey: SymmetricKey) =
    SyncedThread(topic = topic.value, selfAccount = selfAccount.value, peerAccount = peerAccount.value, symmetricKey = symmetricKey.keyAsHex)

internal fun SyncedThread.toCommon(): Pair<Thread, SymmetricKey> = Thread(
    topic = Topic(topic),
    selfAccount = AccountId(selfAccount),
    peerAccount = AccountId(peerAccount)
) to SymmetricKey(symmetricKey)