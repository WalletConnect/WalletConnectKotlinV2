@file:JvmSynthetic

package com.walletconnect.push.common.di

import com.walletconnect.push.common.JsonRpcMethod
import com.walletconnect.push.common.model.PushRpc
import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import org.koin.dsl.module

@JvmSynthetic
internal fun pushJsonRpcModule() = module {

    addSerializerEntry(PushRpc.PushRequest::class)
    addSerializerEntry(PushRpc.PushMessage::class)
    addSerializerEntry(PushRpc.PushDelete::class)
    addSerializerEntry(PushRpc.PushSubscribe::class)

    addDeserializerEntry(JsonRpcMethod.WC_PUSH_REQUEST, PushRpc.PushRequest::class)
    addDeserializerEntry(JsonRpcMethod.WC_PUSH_MESSAGE, PushRpc.PushMessage::class)
    addDeserializerEntry(JsonRpcMethod.WC_PUSH_DELETE, PushRpc.PushDelete::class)
    addDeserializerEntry(JsonRpcMethod.WC_PUSH_SUBSCRIBE, PushRpc.PushSubscribe::class)
}