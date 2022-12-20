package com.walletconnect.auth.di

import com.walletconnect.auth.common.json_rpc.AuthRpc
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod
import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import org.koin.dsl.module

@JvmSynthetic
internal fun jsonRpcModule() = module {

    addSerializerEntry(AuthRpc.AuthRequest::class)

    addDeserializerEntry(JsonRpcMethod.WC_AUTH_REQUEST, AuthRpc.AuthRequest::class)
}