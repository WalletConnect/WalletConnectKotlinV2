@file:JvmSynthetic

package com.walletconnect.chat.di

import com.walletconnect.chat.common.json_rpc.ChatRpc
import com.walletconnect.chat.json_rpc.model.JsonRpcMethod
import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import org.koin.dsl.module
import com.walletconnect.android.impl.di.jsonRpcModule as coreJsonRpcModule

@JvmSynthetic
internal fun jsonRpcModule() = module {

    includes(coreJsonRpcModule())

    addSerializerEntry(ChatRpc.ChatMessage::class)
    addSerializerEntry(ChatRpc.ChatInvite::class)
    addSerializerEntry(ChatRpc.ChatLeave::class)
    addSerializerEntry(ChatRpc.ChatPing::class)

    addDeserializerEntry(JsonRpcMethod.WC_CHAT_MESSAGE, ChatRpc.ChatMessage::class)
    addDeserializerEntry(JsonRpcMethod.WC_CHAT_INVITE, ChatRpc.ChatInvite::class)
    addDeserializerEntry(JsonRpcMethod.WC_CHAT_LEAVE, ChatRpc.ChatLeave::class)
    addDeserializerEntry(JsonRpcMethod.WC_CHAT_PING, ChatRpc.ChatPing::class)
}