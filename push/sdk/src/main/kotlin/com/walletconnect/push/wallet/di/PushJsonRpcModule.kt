@file:JvmSynthetic

package com.walletconnect.push.wallet.di

import com.walletconnect.push.common.model.PushRpc
import com.walletconnect.push.dapp.json_rpc.JsonRpcMethod
import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import com.walletconnect.android.impl.di.jsonRpcModule as coreJsonRpcModule
import org.koin.dsl.module

@JvmSynthetic
internal fun pushJsonRpcModule() = module {

    includes(coreJsonRpcModule())

    addSerializerEntry(PushRpc.PushRequest::class)
    addSerializerEntry(PushRpc.PushMessage::class)

    addDeserializerEntry(JsonRpcMethod.WC_PUSH_REQUEST, PushRpc.PushRequest::class)
    addDeserializerEntry(JsonRpcMethod.WC_PUSH_MESSAGE, PushRpc.PushMessage::class)
}