package com.walletconnect.chat.engine.sync.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.chat.common.model.Invite
import com.walletconnect.chat.common.model.InviteMessage
import com.walletconnect.chat.common.model.InviteStatus
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

@JsonClass(generateAdapter = true)
internal data class SyncedSentInvite(
    val id: Long,
    val inviterAccount: String,
    val inviteeAccount: String,
    val message: String,
    @Json(name = "inviterPubKeyY") val inviterPublicKey: String,
    @Json(name = "inviterPrivKeyY") val inviterPrivateKey: String,
    val status: String,
    @Json(name = "responseTopic") val acceptTopic: String,
    @Json(name = "symKey") val symmetricKey: String,
    val timestamp: Long,
)

internal fun Invite.Sent.toSync(): SyncedSentInvite = SyncedSentInvite(
    id = id,
    inviterAccount = inviterAccount.value,
    inviteeAccount = inviteeAccount.value,
    message = message.value,
    inviterPublicKey = inviterPublicKey.keyAsHex,
    inviterPrivateKey = inviterPrivateKey!!.keyAsHex,
    status = status.name.lowercase(),
    acceptTopic = acceptTopic.value,
    symmetricKey = symmetricKey.keyAsHex,
    timestamp = timestamp
)


internal fun SyncedSentInvite.toCommon(): Invite.Sent = Invite.Sent(
    id = id,
    inviterAccount = AccountId(inviterAccount),
    inviteeAccount = AccountId(inviteeAccount),
    message = InviteMessage(message),
    inviterPublicKey = PublicKey(inviterPublicKey),
    inviterPrivateKey = PrivateKey(inviterPrivateKey),
    status = InviteStatus.valueOf(status.uppercase()),
    acceptTopic = Topic(acceptTopic),
    symmetricKey = SymmetricKey(symmetricKey),
    timestamp = timestamp
)

