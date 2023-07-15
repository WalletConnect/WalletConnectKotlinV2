package com.walletconnect.chat.engine.sync.model

import com.squareup.moshi.JsonClass
import com.walletconnect.chat.common.model.InviteStatus

@JsonClass(generateAdapter = true)
internal data class SyncedReceivedInviteRejectedStatus(
    val id: Long,
    val status: String,
)

internal fun Long.toSync() =
    SyncedReceivedInviteRejectedStatus(id = this, status = InviteStatus.REJECTED.name.lowercase())

internal fun SyncedReceivedInviteRejectedStatus.toCommon(): Pair<Long, InviteStatus> = id to InviteStatus.valueOf(status.uppercase())