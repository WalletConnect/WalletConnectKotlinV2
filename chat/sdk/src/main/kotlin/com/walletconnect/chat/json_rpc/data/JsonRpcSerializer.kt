@file:JvmSynthetic

package com.walletconnect.chat.json_rpc.data

import com.squareup.moshi.Moshi
import com.walletconnect.android.common.SerializableJsonRpc
import com.walletconnect.android.impl.common.model.type.ClientParams
import com.walletconnect.chat.common.json_rpc.ChatRpc
import com.walletconnect.chat.json_rpc.model.JsonRpcMethod

//TODO: Should there be a copy for every SDK with specific methods or one file with all methods.
internal class JsonRpcSerializer(private val moshi: Moshi) {

    internal fun deserialize(method: String, json: String): ClientParams? =
        when (method) {
            JsonRpcMethod.WC_CHAT_MESSAGE -> tryDeserialize<ChatRpc.ChatMessage>(json)?.params
            JsonRpcMethod.WC_CHAT_INVITE -> tryDeserialize<ChatRpc.ChatInvite>(json)?.params
            JsonRpcMethod.WC_CHAT_LEAVE -> tryDeserialize<ChatRpc.ChatLeave>(json)?.params
            JsonRpcMethod.WC_CHAT_PING -> tryDeserialize<ChatRpc.ChatPing>(json)?.params
            else -> null
        }

    fun serialize(payload: SerializableJsonRpc): String =
        when (payload) {
            is ChatRpc.ChatMessage -> trySerialize(payload)
            is ChatRpc.ChatInvite -> trySerialize(payload)
            is ChatRpc.ChatLeave -> trySerialize(payload)
            is ChatRpc.ChatPing -> trySerialize(payload)
            is RelayerDO.JsonRpcResponse.JsonRpcResult -> trySerialize(payload)
            is RelayerDO.JsonRpcResponse.JsonRpcError -> trySerialize(payload)
            else -> String.Empty
        }

    inline fun <reified T> tryDeserialize(json: String): T? = runCatching { moshi.adapter(T::class.java).fromJson(json) }.getOrNull()
    private inline fun <reified T> trySerialize(type: T): String = moshi.adapter(T::class.java).toJson(type)
}