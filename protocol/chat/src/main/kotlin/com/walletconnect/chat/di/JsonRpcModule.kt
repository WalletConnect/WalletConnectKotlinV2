@file:JvmSynthetic

package com.walletconnect.chat.di

import com.walletconnect.chat.common.json_rpc.ChatRpc
import com.walletconnect.chat.json_rpc.JsonRpcMethod
import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import org.koin.dsl.module

@JvmSynthetic
internal fun jsonRpcModule() = module {
    addSerializerEntry(ChatRpc.ChatMessage::class)
    addSerializerEntry(ChatRpc.ChatInvite::class)
    addSerializerEntry(ChatRpc.ChatLeave::class)
    addSerializerEntry(ChatRpc.ChatPing::class)

    addDeserializerEntry(JsonRpcMethod.WC_CHAT_MESSAGE, ChatRpc.ChatMessage::class)
    addDeserializerEntry(JsonRpcMethod.WC_CHAT_INVITE, ChatRpc.ChatInvite::class)
    addDeserializerEntry(JsonRpcMethod.WC_CHAT_LEAVE, ChatRpc.ChatLeave::class)
    addDeserializerEntry(JsonRpcMethod.WC_CHAT_PING, ChatRpc.ChatPing::class)
}