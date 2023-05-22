package com.walletconnect.chat.engine.sync.model

import com.squareup.moshi.JsonClass
import com.walletconnect.chat.common.model.Invite
import com.walletconnect.chat.common.model.InviteStatus

@JsonClass(generateAdapter = true)
internal data class SyncedReceivedInviteStatus(
    val id: Long,
    val status: String,
)

internal fun Invite.Received.toSync() =
    SyncedReceivedInviteStatus(id = id, status = status.name.lowercase())

internal fun SyncedReceivedInviteStatus.toCommon(): Pair<Long, InviteStatus> = id to InviteStatus.valueOf(status.uppercase())