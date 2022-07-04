@file:JvmSynthetic

package com.walletconnect.chat.copiedFromSign.json_rpc.data

import com.squareup.moshi.Moshi
import com.walletconnect.chat.copiedFromSign.core.model.type.ClientParams
import com.walletconnect.chat.copiedFromSign.core.model.type.SerializableJsonRpc
import com.walletconnect.chat.copiedFromSign.json_rpc.model.JsonRpcMethod
import com.walletconnect.chat.copiedFromSign.json_rpc.model.RelayerDO
import com.walletconnect.chat.copiedFromSign.util.Empty
import com.walletconnect.chat.core.model.vo.clientsync.ChatRpcVO

//TODO: Should there be a copy for every SDK with specific methods or one file with all methods.
internal class JsonRpcSerializer(private val moshi: Moshi) {

    internal fun deserialize(method: String, json: String): ClientParams? =
        when (method) {
            JsonRpcMethod.WC_CHAT_MESSAGE -> tryDeserialize<ChatRpcVO.ChatMessage>(json)?.params
            JsonRpcMethod.WC_CHAT_INVITE -> tryDeserialize<ChatRpcVO.ChatInvite>(json)?.params
            JsonRpcMethod.WC_CHAT_LEAVE -> tryDeserialize<ChatRpcVO.ChatLeave>(json)?.params
            JsonRpcMethod.WC_CHAT_PING -> tryDeserialize<ChatRpcVO.ChatPing>(json)?.params
            else -> null
        }

    fun serialize(payload: SerializableJsonRpc): String =
        when (payload) {
            is ChatRpcVO.ChatMessage -> trySerialize(payload)
            is ChatRpcVO.ChatInvite -> trySerialize(payload)
            is ChatRpcVO.ChatLeave -> trySerialize(payload)
            is ChatRpcVO.ChatPing -> trySerialize(payload)
            is RelayerDO.JsonRpcResponse.JsonRpcResult -> trySerialize(payload)
            is RelayerDO.JsonRpcResponse.JsonRpcError -> trySerialize(payload)
            else -> String.Empty
        }

    inline fun <reified T> tryDeserialize(json: String): T? = runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()
    private inline fun <reified T> trySerialize(type: T): String = moshi.adapter(T::class.java).toJson(type)
}