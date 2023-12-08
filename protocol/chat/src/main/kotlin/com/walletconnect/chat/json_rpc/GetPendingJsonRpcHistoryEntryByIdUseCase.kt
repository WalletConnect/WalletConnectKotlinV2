package com.walletconnect.chat.json_rpc

import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.json_rpc.model.JsonRpcHistoryRecord
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.chat.common.json_rpc.ChatRpc
import com.walletconnect.chat.common.model.JsonRpcHistoryEntry
import com.walletconnect.foundation.common.model.Topic

internal class GetPendingJsonRpcHistoryEntryByIdUseCase(
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer,
) {
    operator fun invoke(id: Long): JsonRpcHistoryEntry? {
        val record: JsonRpcHistoryRecord? = jsonRpcHistory.getPendingRecordById(id)
        var entry: JsonRpcHistoryEntry? = null

        if (record != null) {
            val chatInvite: ChatRpc.ChatInvite? = serializer.tryDeserialize<ChatRpc.ChatInvite>(record.body)
            if (chatInvite != null) {
                entry = JsonRpcHistoryEntry.InviteRequest(chatInvite.params, record.id, Topic(record.topic), record.method, record.response)
            }
        }

        return entry
    }
}