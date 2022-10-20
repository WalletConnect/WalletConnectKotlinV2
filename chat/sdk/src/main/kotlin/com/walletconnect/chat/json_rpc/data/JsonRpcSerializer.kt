@file:JvmSynthetic

package com.walletconnect.chat.json_rpc.data

import com.squareup.moshi.Moshi
import com.walletconnect.android.common.SerializableJsonRpc
import com.walletconnect.android.impl.common.model.type.ClientParams
import com.walletconnect.android.impl.json_rpc.data.JsonRpcSerializerAbstract
import com.walletconnect.chat.common.json_rpc.ChatRpc
import com.walletconnect.chat.json_rpc.model.JsonRpcMethod
import com.walletconnect.util.Empty

internal class JsonRpcSerializer(override val moshi: Moshi) : JsonRpcSerializerAbstract(moshi) {

    override fun deserialize(method: String, json: String): ClientParams? =
        when (method) {
            JsonRpcMethod.WC_CHAT_MESSAGE -> tryDeserialize<ChatRpc.ChatMessage>(json)?.params
            JsonRpcMethod.WC_CHAT_INVITE -> tryDeserialize<ChatRpc.ChatInvite>(json)?.params
            JsonRpcMethod.WC_CHAT_LEAVE -> tryDeserialize<ChatRpc.ChatLeave>(json)?.params
            JsonRpcMethod.WC_CHAT_PING -> tryDeserialize<ChatRpc.ChatPing>(json)?.params
            else -> null
        }

    override fun sdkSpecificSerialize(payload: SerializableJsonRpc): String =
        when (payload) {
            is ChatRpc.ChatMessage -> trySerialize(payload)
            is ChatRpc.ChatInvite -> trySerialize(payload)
            is ChatRpc.ChatLeave -> trySerialize(payload)
            is ChatRpc.ChatPing -> trySerialize(payload)
            else -> String.Empty
        }
}