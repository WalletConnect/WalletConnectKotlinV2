@file:JvmSynthetic

package com.walletconnect.push.common.di

import com.walletconnect.push.common.model.PushRpc
import com.walletconnect.push.common.JsonRpcMethod
import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import com.walletconnect.android.impl.di.jsonRpcModule as coreJsonRpcModule
import org.koin.dsl.module

@JvmSynthetic
internal fun pushJsonRpcModule() = module {

//    includes(coreJsonRpcModule())

    addSerializerEntry(PushRpc.PushRequest::class)
    addSerializerEntry(PushRpc.PushMessage::class)
    addSerializerEntry(PushRpc.PushDelete::class)

    addDeserializerEntry(JsonRpcMethod.WC_PUSH_REQUEST, PushRpc.PushRequest::class)
    addDeserializerEntry(JsonRpcMethod.WC_PUSH_MESSAGE, PushRpc.PushMessage::class)
    addDeserializerEntry(JsonRpcMethod.WC_PUSH_DELETE, PushRpc.PushDelete::class)
}