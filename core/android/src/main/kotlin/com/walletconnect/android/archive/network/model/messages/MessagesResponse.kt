package com.walletconnect.android.archive.network.model.messages

import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.ArchiveMessage

@JsonClass(generateAdapter = true)
data class MessagesResponse(
    val topic: String,
    val nextId: String?,
    val messages: List<ArchiveMessage>?,
    val direction: Direction?,
)


