package com.walletconnect.android.history.network.model.messages

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessagesParams(
    val topic: String,
    val originId: Long?,
    val messageCount: Long?,
    val direction: Direction?,
) {
    fun toQueryMap(): Map<String, String> {
        return mutableMapOf("topic" to topic).apply {
            if (originId != null) this["originId"] = originId.toString()
            if (messageCount != null) this["messageCount"] = messageCount.toString()
            if (direction != null) this["direction"] = direction.toString()
        }
    }
}



