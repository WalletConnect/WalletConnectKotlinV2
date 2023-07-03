package com.walletconnect.android.history.network.model.messages

import com.squareup.moshi.JsonClass
import com.walletconnect.android.internal.common.model.HistoryMessage

@JsonClass(generateAdapter = true)
data class MessagesResponse(
    val topic: String,
    val nextId: Long?,
    val messages: List<HistoryMessage>?,
    val direction: Direction?,
)


