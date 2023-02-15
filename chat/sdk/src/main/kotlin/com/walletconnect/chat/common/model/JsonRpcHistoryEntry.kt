@file:JvmSynthetic

package com.walletconnect.chat.common.model

import com.walletconnect.chat.common.json_rpc.ChatParams
import com.walletconnect.foundation.common.model.Topic

internal interface JsonRpcHistoryEntry {
    val id: Long
    val topic: Topic
    val method: String
    val response: String?
    val params: ChatParams

    data class InviteRequest(
        override val params: ChatParams.InviteParams,
        override val id: Long,
        override val topic: Topic,
        override val method: String,
        override val response: String?,
    ) : JsonRpcHistoryEntry
}